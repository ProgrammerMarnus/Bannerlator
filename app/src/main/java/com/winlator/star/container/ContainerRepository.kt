package com.winlator.star.container

import java.io.File

/**
 * Boundary between container management code and the persistence layer.
 *
 * <p>All operations are suspend (coroutine) and return a {@link ContainerResult} instead of
 * throwing across the file/IO boundary, so callers (UI view-models, a future refactored
 * {@link ContainerManager}) can observe outcomes structurally without try/catch noise. Kotlin side
 * of the plan A1.4 interface.
 */
interface ContainerRepository {
    /** All containers, sorted ascending by id. Never null on success. */
    suspend fun getAll(): ContainerResult<List<ContainerConfig>>

    /** A single container by id, or a failure result when missing/unreadable. */
    suspend fun get(id: Int): ContainerResult<ContainerConfig>

    /** Persists a config (create or update). */
    suspend fun save(config: ContainerConfig): ContainerResult<Unit>

    /** Deletes a container by id. */
    suspend fun delete(id: Int): ContainerResult<Unit>

    /** Imports a container from a source directory, assigning a fresh id. */
    suspend fun importContainer(src: File): ContainerResult<ContainerConfig>

    /** Exports a container config to a destination directory. */
    suspend fun exportContainer(config: ContainerConfig, dest: File): ContainerResult<Unit>

    /** Highest assigned container id, or 0 when the set is empty. */
    suspend fun maxContainerId(): ContainerResult<Int>
}