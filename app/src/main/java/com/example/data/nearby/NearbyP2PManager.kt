package com.example.data.nearby

import android.bluetooth.BluetoothAdapter
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class NearbyDevice(
    val id: String,
    val name: String,
    val protocol: String = "Bluetooth / Wi-Fi Direct",
    val rssi: Int = -55,
    val isConnected: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

sealed class NearbyConnectionState {
    object Idle : NearbyConnectionState()
    object Scanning : NearbyConnectionState()
    data class Connected(val device: NearbyDevice) : NearbyConnectionState()
    data class Error(val message: String) : NearbyConnectionState()
}

class NearbyP2PManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = try {
        BluetoothAdapter.getDefaultAdapter()
    } catch (e: Exception) {
        null
    }

    private val _connectionState = MutableStateFlow<NearbyConnectionState>(NearbyConnectionState.Idle)
    val connectionState = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    val discoveredDevices = _discoveredDevices.asStateFlow()

    private val _incomingP2PMessages = MutableStateFlow<Pair<String, String>?>(null)
    val incomingP2PMessages = _incomingP2PMessages.asStateFlow()

    private var activeDevice: NearbyDevice? = null

    init {
        // Pre-populate with typical detectable local peers for demo/testing
        _discoveredDevices.value = listOf(
            NearbyDevice(
                id = "peer_realme_01",
                name = "Realme 11 Pro (Jihad Chat)",
                protocol = "Bluetooth LE Mesh",
                rssi = -48
            ),
            NearbyDevice(
                id = "peer_samsung_02",
                name = "Galaxy A54 (Offline P2P)",
                protocol = "Wi-Fi Direct P2P",
                rssi = -62
            ),
            NearbyDevice(
                id = "peer_redmi_03",
                name = "Redmi Note 13 (Jihad Mesh)",
                protocol = "Bluetooth Rfcomm",
                rssi = -71
            )
        )
    }

    fun startDiscovery(scope: CoroutineScope) {
        _connectionState.value = NearbyConnectionState.Scanning

        scope.launch(Dispatchers.IO) {
            delay(1500)
            // Add dynamically detected peer
            val freshList = _discoveredDevices.value.toMutableList()
            val newPeer = NearbyDevice(
                id = "peer_${UUID.randomUUID().toString().take(6)}",
                name = "Nearby Jihad Phone #" + (100..999).random(),
                protocol = "Wi-Fi Direct & BLE",
                rssi = -52
            )
            if (freshList.none { it.id == newPeer.id }) {
                freshList.add(0, newPeer)
            }
            _discoveredDevices.value = freshList
            if (activeDevice == null) {
                _connectionState.value = NearbyConnectionState.Idle
            }
        }
    }

    fun stopDiscovery() {
        if (_connectionState.value is NearbyConnectionState.Scanning) {
            _connectionState.value = if (activeDevice != null) {
                NearbyConnectionState.Connected(activeDevice!!)
            } else {
                NearbyConnectionState.Idle
            }
        }
    }

    fun connectToDevice(device: NearbyDevice, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val connectedDev = device.copy(isConnected = true)
            activeDevice = connectedDev
            _discoveredDevices.value = _discoveredDevices.value.map {
                if (it.id == device.id) connectedDev else it.copy(isConnected = false)
            }
            _connectionState.value = NearbyConnectionState.Connected(connectedDev)
        }
    }

    fun disconnect() {
        activeDevice = null
        _discoveredDevices.value = _discoveredDevices.value.map { it.copy(isConnected = false) }
        _connectionState.value = NearbyConnectionState.Idle
    }

    fun sendP2PPayload(text: String, scope: CoroutineScope, onDelivered: () -> Unit) {
        scope.launch(Dispatchers.IO) {
            delay(400) // Simulated radio propagation delay over Bluetooth / Wi-Fi Direct
            onDelivered()

            // Simulate friendly echo reply from nearby phone after short delay
            delay(1800)
            val active = activeDevice ?: return@launch
            val echoReplies = listOf(
                "Offline mesh packet received! Connected without internet.",
                "অফলাইন ব্লুটুথ মেসেজ পেয়েছি!",
                "P2P direct link is fast & clear.",
                "আলহামদুলিল্লাহ, অফলাইনেও মেসেজ কাজ করছে!"
            )
            _incomingP2PMessages.value = Pair(active.id, echoReplies.random())
        }
    }
}
