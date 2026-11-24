package io.github.khaledbahaaeldin.emberbyte.data

import kotlinx.coroutines.flow.Flow

data class PermissionState(
    val usageAccess: Boolean,
    val notifications: Boolean,
    val phoneState: Boolean,
    /** True when the app is exempt from battery optimisation (or the system does not report it). */
    val batteryUnrestricted: Boolean = true,
)

interface PermissionRepository {
    fun observe(): Flow<PermissionState>
    suspend fun recheck()
}
