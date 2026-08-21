package com.winlator.star.container

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * File-backed {@link ContainerRepository} over a home directory. Every operation hops to
 * {@code Dispatchers.IO} (so a caller on the main coroutine thread never blocks), delegates to the
 * static JVM harness {@link ContainerIO}, and wraps outcome/exception in {@link ContainerResult}.
 *
 * <p>Scan results are cached and invalidated on any mutating call (save/delete/import), so repeated
 * {@link #getAll()} cheap on the UI thread. Pure JVM — constructible and unit-testable against a
 * transient directory.
 */
class FileContainerRepository(
    private val homeDir: File,
) : ContainerRepository {

    /** Mutable structural cache; invalidated on every mutation. */
    private var cached: List<ContainerConfig>? = null

    override suspend fun getAll(): ContainerResult<List<ContainerConfig>> = withContext(Dispatchers.IO) {
        try {
            if (cached == null) cached = ContainerIO.scanContainerDirs(homeDir)
            ContainerResult.ok(cached ?: emptyList())
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    override suspend fun get(id: Int): ContainerResult<ContainerConfig> = withContext(Dispatchers.IO) {
        try {
            val all = getAll().value ?: emptyList()
            for (c in all) if (c.id == id) return@withContext ContainerResult.ok(c)
            ContainerResult.failure("No container with id $id")
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    override suspend fun save(config: ContainerConfig): ContainerResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = ContainerIO.configFile(homeDir, config.id)
            if (!ContainerIO.save(config, file)) {
                ContainerResult.failure("Failed to write container ${config.id}")
            } else {
                cached = null
                ContainerResult.ok(Unit)
            }
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    override suspend fun delete(id: Int): ContainerResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = ContainerIO.containerDir(homeDir, id)
            if (dir.exists() && !deleteRecursive(dir)) {
                ContainerResult.failure("Failed to delete container dir: ${dir.path}")
            } else {
                cached = null
                ContainerResult.ok(Unit)
            }
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    override suspend fun importContainer(src: File): ContainerResult<ContainerConfig> = withContext(Dispatchers.IO) {
        try {
            val nextId = ContainerIO.maxContainerId(homeDir) + 1
            val dest = ContainerIO.containerDir(homeDir, nextId)
            val imported = ContainerIO.importContainer(src, dest, nextId)
            cached = null
            ContainerResult.ok(imported)
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    override suspend fun exportContainer(config: ContainerConfig, dest: File): ContainerResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!ContainerIO.exportContainer(config, dest)) {
                ContainerResult.failure("Failed to export container ${config.id}")
            } else {
                ContainerResult.ok(Unit)
            }
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    override suspend fun maxContainerId(): ContainerResult<Int> = withContext(Dispatchers.IO) {
        try {
            ContainerResult.ok(ContainerIO.maxContainerId(homeDir))
        } catch (t: Throwable) {
            ContainerResult.failure(t.message ?: t.toString())
        }
    }

    private fun deleteRecursive(dir: File): Boolean {
        val children = dir.listFiles() ?: arrayOf<File>()
        for (child in children) {
            if (child.isDirectory()) {
                if (!deleteRecursive(child)) return false
            } else if (!child.delete()) {
                return false
            }
        }
        return dir.delete()
    }
}