package com.plexo.android.network

enum class NetworkInterfaceType {
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
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
    val ethernet: InterfaceInfo = InterfaceInfo(NetworkInterfaceType.ETHERNET),
    val bluetooth: InterfaceInfo = InterfaceInfo(NetworkInterfaceType.BLUETOOTH),
    val isBondingActive: Boolean = false
) {
    val activeInterfaceCount: Int
        get() = listOf(wifi, cellular, ethernet, bluetooth).count { it.isConnected }

    val activeInterfaces: List<InterfaceInfo>
        get() = listOf(wifi, cellular, ethernet, bluetooth).filter { it.isConnected }
}
