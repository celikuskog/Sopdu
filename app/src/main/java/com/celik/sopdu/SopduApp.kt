package com.celik.sopdu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.celik.sopdu.data.MessageEntity
import com.celik.sopdu.data.PeerEntity
import com.celik.sopdu.data.SopduDb
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun SopduApp(nearbyPeers: List<Peer>, incomingNearbyMessages: List<IncomingNearbyMessage>, connectionStatusByPeer: Map<String, PeerConnectionStatus>, onRequestInit: () -> Unit, onStartScanWindow: () -> Unit, onStopScanWindow: () -> Unit, onSendNearbyMessage: (String, String) -> Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("sopdu_local", Context.MODE_PRIVATE) }
    var showLaunchMark by remember { mutableStateOf(true) }
    var identity by remember { mutableStateOf(loadLocalIdentity(prefs)) }
    var localNicknames by remember { mutableStateOf(loadLocalNicknames(prefs)) }
    var acceptedPeers by remember { mutableStateOf(loadAcceptedPeers(prefs)) }
    var blockedPeers by remember { mutableStateOf(loadBlockedPeers(prefs)) }
    var blockedPeerNames by remember { mutableStateOf(loadBlockedPeerNames(prefs)) }
    var pendingPeers by remember { mutableStateOf(loadPendingPeers(prefs)) }
    var rejectedPeers by remember { mutableStateOf(loadRejectedPeers(prefs)) }
    var distressActive by remember { mutableStateOf(false) }
    var showDistressConfirm by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri != null && json != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                Toast.makeText(context, "Chats exported", Toast.LENGTH_SHORT).show()
            } catch (_: Throwable) {
                Toast.makeText(context, "Export failed", Toast.LENGTH_LONG).show()
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> distressActive = true; if (!granted) Toast.makeText(context, "Flashlight unavailable. Sound distress is active.", Toast.LENGTH_LONG).show() }

    LaunchedEffect(Unit) { delay(1600); showLaunchMark = false }
    if (showLaunchMark) { LaunchBrandScreen(); return }

    val db = remember { SopduDb.get(context) }
    val dao = remember { db.dao() }
    LaunchedEffect(Unit) { onRequestInit() }
    LaunchedEffect(Unit) {
        if (BuildConfig.DEBUG) {
            pendingPeers = seedDebugPendingPeer(prefs)
            val seededHidden = seedDebugHiddenPeers(prefs)
            rejectedPeers = seededHidden.first
            blockedPeers = seededHidden.second
            blockedPeerNames = loadBlockedPeerNames(prefs)
        }
    }
    val peerEntities by dao.observePeers().collectAsState(initial = emptyList())
    val handledIncoming = remember { mutableStateListOf<String>() }
    LaunchedEffect(incomingNearbyMessages.size) {
        incomingNearbyMessages.filterNot { handledIncoming.contains(it.id) }.forEach { incoming ->
            handledIncoming.add(incoming.id)
            val text = cleanIncomingText(incoming.text) ?: return@forEach
            val peerName = incomingPeerName(incoming.peerId, nearbyPeers)
            if (!shouldIgnoreIncomingPeer(incoming.peerId, identity, blockedPeers, rejectedPeers)) {
                if (acceptedPeers.contains(incoming.peerId)) {
                    dao.upsertPeer(PeerEntity(incoming.peerId, peerName, incoming.ts))
                    dao.insertMessage(MessageEntity(incoming.id, incoming.peerId, false, text, incoming.ts, MessageDeliveryStatus.RECEIVED.name))
                } else {
                    pendingPeers = savePendingPeer(prefs, incoming.peerId, peerName)
                }
            }
        }
    }
    LaunchedEffect(peerEntities) {
        if (BuildConfig.DEBUG) seedDebugChatsIfNeeded(dao, peerEntities)
    }
    val chats = peerEntities.filterNot { blockedPeers.contains(it.id) }.map { Peer(it.id, it.name.uppercase(), it.lastSeenAt ?: 0L) }
    var tab by remember { mutableStateOf(Tab.CHATS) }
    var openChat by remember { mutableStateOf<Peer?>(null) }
    var showBrandPage by remember { mutableStateOf(false) }
    var showBrandAbout by remember { mutableStateOf(false) }
    var showHiddenChats by remember { mutableStateOf(false) }
    var toolsOpen by remember { mutableStateOf(false) }
    val batteryMap = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(chats) { chats.forEach { if (!batteryMap.containsKey(it.id)) batteryMap[it.id] = fakeStableBatteryForPeer(it.id) } }
    LaunchedEffect(distressActive) { runDistressSignal(context, distressActive) }

    BackHandler(true) { when { showBrandAbout -> showBrandAbout = false; showBrandPage -> showBrandPage = false; showHiddenChats -> showHiddenChats = false; openChat != null -> openChat = null; tab != Tab.CHATS -> tab = Tab.CHATS; toolsOpen -> toolsOpen = false; else -> (context as? android.app.Activity)?.finish() } }
    fun sendMessage(peer: Peer, text: String) { val now = System.currentTimeMillis(); scope.launch { dao.upsertPeer(PeerEntity(peer.id, peer.name, now)); val sentToTransport = onSendNearbyMessage(peer.id, text); val status = outgoingMessageStatus(sentToTransport, peer); dao.insertMessage(MessageEntity(UUID.randomUUID().toString(), peer.id, true, text, now, status.name)); if (!sentToTransport && !peer.id.startsWith("TEST_") && peer.id != "TEST_PEER") Toast.makeText(context, "Message saved locally. Peer is not connected.", Toast.LENGTH_SHORT).show(); if (peer.id == "TEST_PEER") { dao.insertMessage(MessageEntity(UUID.randomUUID().toString(), peer.id, false, "RECEIVED: $text", System.currentTimeMillis(), MessageDeliveryStatus.RECEIVED.name)); dao.upsertPeer(PeerEntity(peer.id, peer.name, System.currentTimeMillis())) } } }
    var btOn by remember { mutableStateOf(false) }; var tetheringOn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { btOn = isBluetoothEnabled(); tetheringOn = isTetheringOnBestEffort(context); delay(1000) } }

    val bleScanner = remember { BleRadarScanner(context) }
    var isScanning by remember { mutableStateOf(false) }; var scanLeft by remember { mutableStateOf(0) }; val scanSeconds = 15
    var hideNonSopdu by remember { mutableStateOf(false) }; var pinned by remember { mutableStateOf<RadarHit?>(null) }
    fun clearScanResults() { bleScanner.clear(); pinned = null }
    fun stopScanNow() { if (!isScanning) return; bleScanner.stop(); onStopScanWindow(); isScanning = false; scanLeft = 0; clearScanResults() }
    fun beginScanWindow() { if (isScanning) return; clearScanResults(); isScanning = true; scanLeft = scanSeconds; onStartScanWindow(); bleScanner.start(); scope.launch { while (scanLeft > 0) { delay(1000); scanLeft -= 1 }; stopScanNow() } }
    fun openDiscoveredSopduPeer(hit: RadarHit) { val now = System.currentTimeMillis(); val name = hit.title.uppercase(); if (blockedPeers.contains(hit.idKey)) { Toast.makeText(context, "This peer is blocked.", Toast.LENGTH_SHORT).show(); return }; acceptedPeers = saveAcceptedPeer(prefs, hit.idKey); pendingPeers = removePendingPeer(prefs, hit.idKey); rejectedPeers = removeRejectedPeer(prefs, hit.idKey); scope.launch { dao.upsertPeer(PeerEntity(hit.idKey, name, now)); openChat = Peer(hit.idKey, name, now); stopScanNow() } }
    fun acceptPending(peerId: String, name: String) { val now = System.currentTimeMillis(); acceptedPeers = saveAcceptedPeer(prefs, peerId); pendingPeers = removePendingPeer(prefs, peerId); rejectedPeers = removeRejectedPeer(prefs, peerId); scope.launch { dao.upsertPeer(PeerEntity(peerId, name, now)); openChat = Peer(peerId, name, now); showHiddenChats = false } }
    fun rejectPending(peerId: String) { val name = pendingPeers[peerId] ?: peerId.take(6).uppercase(); rejectedPeers = saveRejectedPeer(prefs, peerId, name); pendingPeers = removePendingPeer(prefs, peerId) }
    fun acceptRejected(peerId: String, name: String) { val now = System.currentTimeMillis(); acceptedPeers = saveAcceptedPeer(prefs, peerId); rejectedPeers = removeRejectedPeer(prefs, peerId); scope.launch { dao.upsertPeer(PeerEntity(peerId, name, now)); openChat = Peer(peerId, name, now); showHiddenChats = false } }
    fun removeRejected(peerId: String) { rejectedPeers = removeRejectedPeer(prefs, peerId) }
    fun blockPeer(peerId: String) { val name = peerEntities.find { it.id == peerId }?.name?.uppercase() ?: openChat?.takeIf { it.id == peerId }?.name ?: pendingPeers[peerId] ?: peerId.take(6).uppercase(); blockedPeers = saveBlockedPeer(prefs, peerId, name); blockedPeerNames = loadBlockedPeerNames(prefs); pendingPeers = removePendingPeer(prefs, peerId); rejectedPeers = removeRejectedPeer(prefs, peerId); if (openChat?.id == peerId) openChat = null }
    fun unblockPeer(peerId: String) { blockedPeers = removeBlockedPeer(prefs, peerId); blockedPeerNames = loadBlockedPeerNames(prefs) }
    fun exportChats() { scope.launch { val json = buildChatsExportJson(identity, dao.getAllPeers(), dao.getAllMessages(), localNicknames); pendingExportJson = json; val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date()); exportLauncher.launch("Sopdu-export-$stamp.json") } }
    fun clearAllMessages() { scope.launch { dao.deleteAllMessages(); Toast.makeText(context, "Messages cleared", Toast.LENGTH_SHORT).show() } }
    fun deleteChat(peerId: String) { scope.launch { dao.deleteMessagesForPeer(peerId); dao.deletePeer(peerId); localNicknames = saveLocalNickname(prefs, peerId, ""); pendingPeers = removePendingPeer(prefs, peerId); rejectedPeers = removeRejectedPeer(prefs, peerId); blockedPeers = removeBlockedPeer(prefs, peerId); blockedPeerNames = loadBlockedPeerNames(prefs); if (openChat?.id == peerId) openChat = null } }
    LaunchedEffect(tab) { if (tab != Tab.NEARBY) { stopScanNow(); clearScanResults() }; if (tab != Tab.CHATS) toolsOpen = false }
    val btShown = if (!isScanning || hideNonSopdu) emptyList() else bleScanner.otherDevices.toList()
    val radarSopduPeers = when {
        !isScanning -> emptyList()
        BuildConfig.DEBUG && nearbyPeers.isEmpty() -> listOf(Peer("DEBUG_NEARBY_SOPDU", "SOPDU TEST"))
        else -> nearbyPeers
    }

    Scaffold(containerColor = BG, bottomBar = { if (openChat == null && !showBrandPage && !showBrandAbout && !showHiddenChats) BottomNavigationBarIcons(tab) { tab = it } }) { inner ->
        Box(Modifier.padding(inner).fillMaxSize().background(BG)) {
            when {
                showBrandAbout -> AboutScreen(onBack = { showBrandAbout = false })
                showBrandPage -> BrandHomeScreen(onBack = { showBrandPage = false }, onAbout = { showBrandAbout = true })
                showHiddenChats -> HiddenChatsScreen(rejectedPeers, blockedPeerNames, onBack = { showHiddenChats = false }, onAcceptRejected = ::acceptRejected, onRemoveRejected = ::removeRejected, onUnblockPeer = ::unblockPeer)
                openChat != null -> { val peer = openChat!!; val peerId = peer.id; val messageEntities by dao.observeMessages(peerId).collectAsState(initial = emptyList()); val msgs = messageEntities.map(::messageEntityToChatMsg); ChatScreen(peer, peerEntities.find { it.id == peerId }?.name?.uppercase() ?: peer.name, msgs, onBack = { openChat = null }, onSend = { sendMessage(peer, it) }, onSaveLocalName = { localNicknames = saveLocalNickname(prefs, peer.id, it) }, onDeleteChat = { deleteChat(peer.id) }, onBlockPeer = { blockPeer(peer.id) }, additionalName = localNicknames[peerId].orEmpty(), batteryPercent = batteryMap[peerId], lastSeenLabel = formatLastSeen(peer.lastSeenAt), distressStatus = if (distressActive) "ACTIVE" else "NORMAL", connectionStatus = connectionStatusByPeer[peerId] ?: PeerConnectionStatus.UNKNOWN) }
                tab == Tab.CHATS -> { ChatsScreen(chats, pendingPeers.filterKeys { !blockedPeers.contains(it) && !rejectedPeers.containsKey(it) }, rejectedPeers.size + blockedPeerNames.size, dao, { batteryMap[it] }, { localNicknames[it].orEmpty() }, onLogoClick = { showBrandPage = true; toolsOpen = false }, onAcceptPending = ::acceptPending, onRejectPending = ::rejectPending, onOpenHiddenChats = { showHiddenChats = true; toolsOpen = false }) { openChat = it }; TopRightToolsPopout(toolsOpen, { toolsOpen = !toolsOpen }, btOn, tetheringOn, { openBluetoothSettings(context) }, { openTetheringSettings(context) }, { openDialer(context) }, distressActive) { if (distressActive) distressActive = false else showDistressConfirm = true } }
                tab == Tab.NEARBY -> NearbyRadarScreen(radarSopduPeers, btShown, hideNonSopdu, { hideNonSopdu = it }, isScanning, scanLeft, scanSeconds, { beginScanWindow() }, { stopScanNow() }, pinned, { hit -> pinned = if (pinned?.idKey == hit.idKey) null else hit }, ::openDiscoveredSopduPeer)
                tab == Tab.SETTINGS -> SettingsScreen(identity, ::exportChats, ::clearAllMessages)
            }
        }
    }
    if (showDistressConfirm) DistressConfirmDialog(onConfirm = { showDistressConfirm = false; if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) distressActive = true else cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }, onDismiss = { showDistressConfirm = false })
}
