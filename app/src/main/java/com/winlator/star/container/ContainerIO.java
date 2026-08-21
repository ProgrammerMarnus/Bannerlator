package com.winlator.star.container;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Static file/IO harness for {@link ContainerConfig} persistence. Pure JVM ({@code java.io} +
 * {@code org.json}) so the JVM unit tests can save/load/import/export/scan against a real transient
 * directory. The on-device legacy path uses android {@code FileUtils}; this type declares the same
 * data contract so switching the backing store later is mechanical.
 *
 * <p>Design intent (plan A1.3): methods return values directly and let the CALLER surface failure
 * (the repository wraps results in {@link ContainerResult}). No instance state.
 */
public final class ContainerIO {
    /** Per-container JSON filename the runtime already uses. */
    public static final String CONFIG_FILE_NAME = ".container";

    private ContainerIO() {
        // static utility only
    }

    /** Persist a config to the given file. Returns false on any write failure. */
    public static boolean save(ContainerConfig config, File file) {
        if (file == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;
        try {
            byte[] bytes = config.toJsonString().getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), bytes);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Load a config from a file, propagating IOException/JSONException to the caller. */
    public static ContainerConfig load(File file) throws IOException, JSONException {
        if (file == null || !file.isFile()) {
            throw new IOException("Container config not found: "
                    + (file == null ? "null" : file.getPath()));
        }
        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        return ContainerConfig.fromJson(json);
    }

    /** Config file path for a container id under a home dir, mirroring {@code xuser-<id>/.container}. */
    public static File configFile(File homeDir, int id) {
        return new File(containerDir(homeDir, id), CONFIG_FILE_NAME);
    }

    /** Container root dir for an id, mirroring {@code xuser-<id>}. */
    public static File containerDir(File homeDir, int id) {
        return new File(homeDir, "xuser-" + id);
    }
    public static ContainerConfig importContainer(File srcDir, File destDir, int newId)
            throws IOException, JSONException {
        if (srcDir == null || !srcDir.isDirectory()) {
            throw new IOException("Invalid container dir for import: "
                    + (srcDir == null ? "null" : srcDir.getPath()));
        }
        if (destDir.exists() && destDir.listFiles() != null && destDir.listFiles().length != 0) {
            throw new IOException("Destination already contains files: " + destDir.getPath());
        }
        copyTree(srcDir.toPath(), destDir.toPath());
        ContainerConfig imported = load(new File(destDir, CONFIG_FILE_NAME));
        ContainerConfig rebased = rebuildWithId(imported, newId);
        save(rebased, new File(destDir, CONFIG_FILE_NAME));
        return rebased;
    }

    /** Export a container's config to a destination dir. Fails if the dir is non-empty. */
    public static boolean exportContainer(ContainerConfig config, File destDir) {
        if (config == null || destDir == null) return false;
        if (destDir.exists() && destDir.listFiles() != null && destDir.listFiles().length != 0) {
            return false;
        }
        return save(config, new File(destDir, CONFIG_FILE_NAME));
    }

    /**
     * Scan all {@code xuser-<id>} dirs under a home dir and return their configs, ascending by id.
     * Unreadable/corrupt entries are skipped so one bad container can't take down the list.
     */
    public static List<ContainerConfig> scanContainerDirs(File homeDir) {
        List<ContainerConfig> result = new ArrayList<>();
        if (homeDir == null || !homeDir.isDirectory()) return result;
        List<Integer> ids = new ArrayList<>();
        File[] files = homeDir.listFiles();
        if (files == null) return result;
        for (File f : files) {
            Integer id = parseContainerId(f);
            if (id != null && f.isDirectory()) ids.add(id);
        }
        ids.sort(Comparator.naturalOrder());
        for (int id : ids) {
            try {
                result.add(load(configFile(homeDir, id)));
            } catch (Exception e) {
                // skip corrupt/unreadable entry
            }
        }
        return result;
    }

    /** Highest assigned container id under a home dir, or 0 if none. */
    public static int maxContainerId(File homeDir) {
        int max = 0;
        if (homeDir != null && homeDir.isDirectory()) {
            File[] files = homeDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    Integer id = parseContainerId(f);
                    if (id != null && id > max) max = id;
                }
            }
        }
        return max;
    }

    public static boolean migrateLegacyPrefs(Map<String, ?> prefs, Map<String, Object> out) {
        Objects.requireNonNull(prefs, "prefs");
        Objects.requireNonNull(out, "out");
        if (Boolean.TRUE.equals(prefs.get("gyro_migrated_to_container"))) return false;

        String[] gyroKeys = {
                "gyro_enabled", "gyro_target", "gyro_activator", "gyro_sensitivity",
                "gyro_deadzone", "gyro_smoothing", "gyro_invert_x", "gyro_invert_y"
        };
        boolean any = false;
        for (String key : gyroKeys) {
            if (prefs.containsKey(key)) {
                any = true;
                break;
            }
        }
        if (!any) return false;

        boolean enabled = asBool(prefs.get("gyro_enabled"), false);
        int target = asInt(prefs.get("gyro_target"));
        int activator = asInt(prefs.get("gyro_activator"));
        float sensitivity = asFloat(prefs.get("gyro_sensitivity"));
        float deadzone = asFloat(prefs.get("gyro_deadzone"));
        float smoothing = asFloat(prefs.get("gyro_smoothing"));
        boolean invertX = asBool(prefs.get("gyro_invert_x"), false);
        boolean invertY = asBool(prefs.get("gyro_invert_y"), false);

        out.put("gyroEnabled", enabled ? "1" : "0");
        out.put("gyroTarget", String.valueOf(target));
        out.put("gyroActivator", String.valueOf(activator));
        out.put("gyroSensitivity", String.valueOf(sensitivity));
        out.put("gyroDeadzone", String.valueOf(deadzone));
        out.put("gyroSmoothing", String.valueOf(smoothing));
        out.put("gyroInvertX", invertX ? "1" : "0");
        out.put("gyroInvertY", invertY ? "1" : "0");
        out.put("gyro_migrated_to_container", true);
        return true;
    }

    // ── internals ────────────────────────────────────────────────────────────────────────
    private static ContainerConfig rebuildWithId(ContainerConfig c, int id) throws JSONException {
        JSONObject json = c.toJson();
        json.put("id", id);
        return ContainerConfig.fromJson(json);
    }

    private static Integer parseContainerId(File f) {
        if (f == null) return null;
        String n = f.getName();
        String prefix = "xuser-";
        if (!n.startsWith(prefix)) return null;
        try {
            return Integer.valueOf(n.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void copyTree(Path src, Path dst) throws IOException {
        try (Stream<Path> stream = Files.walk(src)) {
            List<Path> sources = stream.toList();
            for (Path source : sources) {
                if (source.equals(src)) {
                    if (!Files.exists(dst)) Files.createDirectories(dst);
                    continue;
                }
                Path target = dst.resolve(src.relativize(source));
                if (Files.isSymbolicLink(source)) {
                    try {
                        Files.createSymbolicLink(target, Files.readSymbolicLink(source));
                    } catch (Exception e) {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } else if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static boolean asBool(Object v, boolean dflt) {
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) {
            return "1".equals(v) || "true".equalsIgnoreCase(((String) v).trim());
        }
        if (v instanceof Number) return ((Number) v).intValue() != 0;
        return dflt;
    }

    private static int asInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try {
                return Integer.parseInt(((String) v).trim());
            } catch (NumberFormatException ignored) { }
        }
        return 0;
    }

    private static float asFloat(Object v) {
        if (v instanceof Number) return ((Number) v).floatValue();
        if (v instanceof String) {
            try {
                return Float.parseFloat(((String) v).trim());
            } catch (NumberFormatException ignored) { }
        }
        return 0f;
    }
}