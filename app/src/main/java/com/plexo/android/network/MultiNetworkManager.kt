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
    private var wifiClient: OkHttpClient? = null

    @Volatile
    private var cellularClient: OkHttpClient? = null

    private val defaultClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var wifiCallback: ConnectivityManager.NetworkCallback? = null
    private var cellularCallback: ConnectivityManager.NetworkCallback? = null

    fun startListening() {
        registerWifiCallback()
        registerCellularCallback()
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
                    current.copy(
                        wifi = current.wifi.copy(isConnected = true, ipAddress = ip),
                        isBondingActive = current.cellular.isConnected
                    )
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Wi-Fi network lost: $network")
                if (wifiNetwork == network) {
                    wifiNetwork = null
                    wifiClient = null
                    _networkState.update { current ->
                        current.copy(
                            wifi = current.wifi.copy(isConnected = false, ipAddress = null),
                            isBondingActive = false
                        )
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
                    current.copy(
                        cellular = current.cellular.copy(isConnected = true, ipAddress = ip),
                        isBondingActive = current.wifi.isConnected
                    )
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Cellular network lost: $network")
                if (cellularNetwork == network) {
                    cellularNetwork = null
                    cellularClient = null
                    _networkState.update { current ->
                        current.copy(
                            cellular = current.cellular.copy(isConnected = false, ipAddress = null),
                            isBondingActive = false
                        )
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
            NetworkInterfaceType.DEFAULT -> defaultClient
        }
    }

    fun hasBothNetworks(): Boolean {
        return wifiClient != null && cellularClient != null
    }

    fun stopListening() {
        try {
            wifiCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            cellularCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering callbacks", e)
        }
    }

    companion object {
        private const val TAG = "MultiNetworkManager"
    }
}
