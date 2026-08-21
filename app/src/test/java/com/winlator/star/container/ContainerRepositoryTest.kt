package com.winlator.star.container

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlinx.coroutines.runBlocking

/**
 * JVM coverage for {@link FileContainerRepository} (the suspend repository over a real transient
 * directory). Drives the coroutine API with {@code runBlocking} — no android mock needed because
 * every operation lands on the pure-JVM {@link ContainerIO}.
 */
class FileContainerRepositoryTest {

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder()

    private fun ctor(id: Int, name: String): ContainerConfig =
        ContainerConfig.builder(id).name(name).screenSize("1280x720").build()

    @Test
    fun saveAndLoad_getAllContainsIt() = runBlocking {
        val repo = FileContainerRepository(tempFolder.newFolder("home"))
        val cfg = ctor(1, "Alpha")
        assertEquals(true, repo.save(cfg).isSuccess)
        val all = repo.getAll().value ?: emptyList()
        assertEquals(1, all.size)
        assertEquals("Alpha", all[0].name)
    }

    @Test
    fun delete_removesFromList() = runBlocking {
        val repo = FileContainerRepository(tempFolder.newFolder("home"))
        repo.save(ctor(1, "A"))
        assertEquals(true, repo.delete(1).isSuccess)
        val all = repo.getAll().value ?: emptyList()
        assertTrue(all.isEmpty())
    }

    @Test
    fun importExport_reimportRoundTrips() = runBlocking {
        val home = tempFolder.newFolder("home")
        val repo = FileContainerRepository(home)

        // Build a source container dir: an id dir with a .container file.
        val srcRoot = tempFolder.newFolder("src")
        val srcContainer = File(srcRoot, "xuser-9")
        srcContainer.mkdirs()
        ContainerIO.save(ctor(9, "Imported"), File(srcContainer, ".container"))

        val imported = repo.importContainer(srcContainer)
        assertEquals(true, imported.isSuccess)
        val importedVal = imported.value
        assertTrue(importedVal != null)

        // home was empty -> next id = 1; the imported config must round-trip.
        val all = repo.getAll().value ?: emptyList()
        assertEquals(1, all.size)
        assertEquals("Imported", all[0].name)
    }

    @Test
    fun errorHandling_corruptJsonIsSkipped() = runBlocking {
        val home = tempFolder.newFolder("home")
        val badDir = File(home, "xuser-5")
        badDir.mkdirs()
        // Corrupt JSON -> must not blow up the scan; getAll returns an empty success list.
        File(badDir, ".container").writeText("not json {{{")
        val repo = FileContainerRepository(home)
        val result = repo.getAll()
        assertTrue(result.isSuccess)
        assertTrue((result.value ?: emptyList()).isEmpty())
    }

    @Test
    fun listing_sortedById() = runBlocking {
        val repo = FileContainerRepository(tempFolder.newFolder("home"))
        repo.save(ctor(7, "Seven"))
        repo.save(ctor(2, "Two"))
        repo.save(ctor(4, "Four"))
        val ids = (repo.getAll().value ?: emptyList()).map { it.id }
        assertEquals(java.util.Arrays.asList(2, 4, 7), ids)
    }
}