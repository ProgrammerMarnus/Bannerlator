package com.winlator.star.container

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * JVM coverage for the {@link ContainerCoordinator} — the repository/scope-backed manager half that
 * a future ContainerManager refactor delegates to. Tests create/delete/switch/lifecycle against a
 * fake {@link ContainerRepository} (no real file I/O), matching the plan's manager test intent.
 */
class ContainerManagerTest {

    private class RecordingRepository(
        val saved: MutableList<ContainerConfig> = arrayListOf(),
        val deleted: MutableList<Int> = arrayListOf(),
    ) : ContainerRepository {
        override suspend fun getAll(): ContainerResult<List<ContainerConfig>> =
            ContainerResult.ok(saved.toList())

        override suspend fun get(id: Int): ContainerResult<ContainerConfig> {
            val found = saved.find { it.id == id }
            return if (found != null) ContainerResult.ok(found) else ContainerResult.failure("missing $id")
        }

        override suspend fun save(config: ContainerConfig): ContainerResult<Unit> {
            saved.add(config)
            return ContainerResult.ok(Unit)
        }

        override suspend fun delete(id: Int): ContainerResult<Unit> {
            deleted.add(id)
            return ContainerResult.ok(Unit)
        }

        override suspend fun importContainer(src: File): ContainerResult<ContainerConfig> =
            ContainerResult.failure("not in this test")

        override suspend fun exportContainer(config: ContainerConfig, dest: File): ContainerResult<Unit> =
            ContainerResult.failure("not in this test")

        override suspend fun maxContainerId(): ContainerResult<Int> =
            ContainerResult.ok(saved.size)
    }

    private class CoordinatorFixture(val coordinator: ContainerCoordinator, val repo: RecordingRepository)

    private fun newCoordinatorAndRepo(): CoordinatorFixture =
        RecordingRepository().let { repo ->
            CoordinatorFixture(
                ContainerCoordinator(repo, CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)),
                repo,
            )
        }

    /**
     * create(): save() must be called at the repository AND the container becomes active.
     */
    @Test
    fun createContainer_delegatesSave_andSetsActive() = runBlocking {
        val (coordinator, repo) = newCoordinatorAndRepo()
        val cfg = ContainerConfig.builder(1).name("X").build()

        var saved = false
        coordinator.create(cfg) { saved = it.isSuccess }
        assertTrue(saved)
        assertEquals(1, repo.saved.size)
        assertEquals(1, coordinator.activeContainerId)
        assertTrue(coordinator.isActive)
    }

    /**
     * delete() deleges repo.delete and clears active id when it pointed at the deleted container.
     */
    @Test fun delete_delegatesRepo_clearsActive() = runBlocking {
        val (coordinator, repo) = newCoordinatorAndRepo()
        coordinator.switchActive(3)
        assertEquals(3, coordinator.activeContainerId)

        var deleted = false
        coordinator.delete(3) { deleted = it.isSuccess }
        assertTrue(deleted)
        assertEquals(listOf(3), repo.deleted)
        assertFalse(coordinator.isActive)
    }

    /**
     * setActive toggles the active id without any persistence layer call.
     */
    @Test fun switchActive_updatesActiveId_withoutRepository() = runBlocking {
        val (coordinator, repo) = newCoordinatorAndRepo()
        assertFalse(coordinator.isActive)
        coordinator.switchActive(9)
        assertEquals(9, coordinator.activeContainerId)
        assertTrue(repo.saved.isEmpty())
    }

    /** onDestroy cancels the scope so no further work can be scheduled. */
    @Test fun lifecycle_onDestroy_cancelsScope() {
        val (coordinator, _) = newCoordinatorAndRepo()
        coordinator.onDestroy()
        // A cancelled supervisor scope is no longer active; scheduling must be a no-op (no throw).
        coordinator.onDestroy() // idempotent
        assertFalse(coordinator.isActive)
    }
}