package com.plexo.android.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import java.net.Inet4Address
import java.util.concurrent.TimeUnit

class MultiNetworkManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(NetworkBondingState())
    val networkState: StateFlow<NetworkBondingState> = _networkState.asStateFlow()

    @Volatile
    private var wifiNetwork: Network? = null

    @Volatile
    private var cellularNetwork: Network? = null

    @Volatile
    private var ethernetNetwork: Network? = null

    @Volatile
    private var bluetoothNetwork: Network? = null

    @Volatile
    private var wifiClient: OkHttpClient? = null

    @Volatile
    private var cellularClient: OkHttpClient? = null

    @Volatile
    private var ethernetClient: OkHttpClient? = null

    @Volatile
    private var bluetoothClient: OkHttpClient? = null

    private val defaultClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var wifiCallback: ConnectivityManager.NetworkCallback? = null
    private var cellularCallback: ConnectivityManager.NetworkCallback? = null
    private var ethernetCallback: ConnectivityManager.NetworkCallback? = null
    private var bluetoothCallback: ConnectivityManager.NetworkCallback? = null

    fun startListening() {
        registerWifiCallback()
        registerCellularCallback()
        registerEthernetCallback()
        registerBluetoothCallback()
    }

    private fun registerWifiCallback() {
        val wifiRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        wifiCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Wi-Fi network available: $network")
                wifiNetwork = network
                wifiClient = OkHttpClient.Builder()
                    .socketFactory(network.socketFactory)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val ip = resolveIp(network)
                _networkState.update { current ->
                    val updated = current.copy(
                        wifi = current.wifi.copy(isConnected = true, ipAddress = ip)
                    )
                    updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Wi-Fi network lost: $network")
                if (wifiNetwork == network) {
                    wifiNetwork = null
                    wifiClient = null
                    _networkState.update { current ->
                        val updated = current.copy(
                            wifi = current.wifi.copy(isConnected = false, ipAddress = null)
                        )
                        updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                    }
                }
            }
        }

        try {
            connectivityManager.requestNetwork(wifiRequest, wifiCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Wi-Fi network", e)
        }
    }

    private fun registerCellularCallback() {
        // Requesting cellular network forces Android to keep mobile data connection active
        // even when connected to Wi-Fi.
        val cellularRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cellularCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Cellular network available: $network")
                cellularNetwork = network
                cellularClient = OkHttpClient.Builder()
                    .socketFactory(network.socketFactory)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val ip = resolveIp(network)
                _networkState.update { current ->
                    val updated = current.copy(
                        cellular = current.cellular.copy(isConnected = true, ipAddress = ip)
                    )
                    updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Cellular network lost: $network")
                if (cellularNetwork == network) {
                    cellularNetwork = null
                    cellularClient = null
                    _networkState.update { current ->
                        val updated = current.copy(
                            cellular = current.cellular.copy(isConnected = false, ipAddress = null)
                        )
                        updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                    }
                }
            }
        }

        try {
            connectivityManager.requestNetwork(cellularRequest, cellularCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting cellular network", e)
        }
    }

    private fun registerEthernetCallback() {
        val ethernetRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        ethernetCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Ethernet network available: $network")
                ethernetNetwork = network
                ethernetClient = OkHttpClient.Builder()
                    .socketFactory(network.socketFactory)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val ip = resolveIp(network)
                _networkState.update { current ->
                    val updated = current.copy(
                        ethernet = current.ethernet.copy(isConnected = true, ipAddress = ip)
                    )
                    updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Ethernet network lost: $network")
                if (ethernetNetwork == network) {
                    ethernetNetwork = null
                    ethernetClient = null
                    _networkState.update { current ->
                        val updated = current.copy(
                            ethernet = current.ethernet.copy(isConnected = false, ipAddress = null)
                        )
                        updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                    }
                }
            }
        }

        try {
            connectivityManager.requestNetwork(ethernetRequest, ethernetCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Ethernet network", e)
        }
    }

    private fun registerBluetoothCallback() {
        val bluetoothRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        bluetoothCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Bluetooth network available: $network")
                bluetoothNetwork = network
                bluetoothClient = OkHttpClient.Builder()
                    .socketFactory(network.socketFactory)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val ip = resolveIp(network)
                _networkState.update { current ->
                    val updated = current.copy(
                        bluetooth = current.bluetooth.copy(isConnected = true, ipAddress = ip)
                    )
                    updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Bluetooth network lost: $network")
                if (bluetoothNetwork == network) {
                    bluetoothNetwork = null
                    bluetoothClient = null
                    _networkState.update { current ->
                        val updated = current.copy(
                            bluetooth = current.bluetooth.copy(isConnected = false, ipAddress = null)
                        )
                        updated.copy(isBondingActive = updated.activeInterfaceCount >= 2)
                    }
                }
            }
        }

        try {
            connectivityManager.requestNetwork(bluetoothRequest, bluetoothCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Bluetooth network", e)
        }
    }

    private fun resolveIp(network: Network): String? {
        val linkProperties = connectivityManager.getLinkProperties(network) ?: return null
        return linkProperties.linkAddresses
            .firstOrNull { it.address is Inet4Address }
            ?.address?.hostAddress
    }

    fun getClientForType(type: NetworkInterfaceType): OkHttpClient {
        return when (type) {
            NetworkInterfaceType.WIFI -> wifiClient ?: defaultClient
            NetworkInterfaceType.CELLULAR -> cellularClient ?: defaultClient
            NetworkInterfaceType.ETHERNET -> ethernetClient ?: defaultClient
            NetworkInterfaceType.BLUETOOTH -> bluetoothClient ?: defaultClient
            NetworkInterfaceType.DEFAULT -> defaultClient
        }
    }

    fun getAvailableTransports(): List<NetworkInterfaceType> {
        val list = mutableListOf<NetworkInterfaceType>()
        if (wifiClient != null) list.add(NetworkInterfaceType.WIFI)
        if (cellularClient != null) list.add(NetworkInterfaceType.CELLULAR)
        if (ethernetClient != null) list.add(NetworkInterfaceType.ETHERNET)
        if (bluetoothClient != null) list.add(NetworkInterfaceType.BLUETOOTH)
        return list
    }

    fun hasMultipleNetworks(): Boolean {
        return getAvailableTransports().size >= 2
    }

    fun hasBothNetworks(): Boolean {
        return hasMultipleNetworks()
    }

    fun stopListening() {
        try {
            wifiCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            cellularCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            ethernetCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            bluetoothCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering callbacks", e)
        }
    }

    companion object {
        private const val TAG = "MultiNetworkManager"
    }
}
