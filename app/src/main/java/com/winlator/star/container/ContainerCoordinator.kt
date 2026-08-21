package com.winlator.star.container

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Small repository-backed coordinator for the container list — the view-model-facing half of the
 * phase-1 boundary. It holds the "active container" notion and runs {@code create}/{@code delete}
 * on a supervisor coroutine scope so a future refactor of {@code ContainerManager} can delegate
 * here instead of owning file I/O + raw executors. Pure JVM; the coroutine scope makes it
 * testable with a fake {@link ContainerRepository}.
 */
class ContainerCoordinator(
    private val repository: ContainerRepository,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Unconfined),
) {
    private val lifecycle = scope
    private var activeId: Int? = null

    val activeContainerId: Int?
        get() = activeId

    /** Saves a container (create or persist) and makes it active on success. */
    fun create(config: ContainerConfig, onResult: (ContainerResult<Unit>) -> Unit) {
        lifecycle.launch {
            val result = repository.save(config)
            if (result.isSuccess) activeId = config.id
            onResult(result)
        }
    }

    /** Deletes by id; clears the active id if it pointed at the removed container. */
    fun delete(id: Int, onResult: (ContainerResult<Unit>) -> Unit) {
        lifecycle.launch {
            val result = repository.delete(id)
            if (result.isSuccess && activeId == id) activeId = null
            onResult(result)
        }
    }

    /** Switches the active container id (no persistence). */
    fun switchActive(id: Int) {
        activeId = id
    }

    /** Cancels the scope (teardown hook — mirrors ContainerManager#onDestroy). */
    fun onDestroy() {
        lifecycle.cancel()
    }

    val isActive: Boolean
        get() = activeId != null
}