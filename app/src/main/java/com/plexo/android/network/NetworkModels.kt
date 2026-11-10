package com.plexo.android.network

enum class NetworkInterfaceType {
    WIFI,
    CELLULAR,
    DEFAULT
}

data class InterfaceInfo(
    val type: NetworkInterfaceType,
    val isConnected: Boolean = false,
    val ipAddress: String? = null,
    val speedBps: Long = 0L
)

data class NetworkBondingState(
    val wifi: InterfaceInfo = InterfaceInfo(NetworkInterfaceType.WIFI),
    val cellular: InterfaceInfo = InterfaceInfo(NetworkInterfaceType.CELLULAR),
    val isBondingActive: Boolean = false
)
