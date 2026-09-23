package com.example.radioprueba

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

data class RadioStation(
    val id: Int,
    val name: String,
    val frequency: String,
    val genre: String,
    val bitrate: String,
    val isOfficial: Boolean,
    val streamUrl: String
)

data class GenreCategory(
    val name: String,
    val colors: List<Color>
)

val sampleStations = listOf(
    RadioStation(
        1,
        "W Radio",
        "99.9 FM",
        "Debate / Noticias / Música",
        "AAC+ • 128 kbps",
        true,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/W_RADIO_SC.mp3"
    ),
    RadioStation(
        2,
        "La FM",
        "98.5 FM",
        "Noticias / Pop / Rock",
        "AAC+ • 96 kbps",
        true,
        "https://icecast.rcnradio.com/lafm.mp3"
    ),
    RadioStation(
        3,
        "La Kalle",
        "96.9 FM",
        "Popular / Reggaeton",
        "AAC+ • 128 kbps",
        true,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/LA_KALLE_SC.mp3"
    ),
    RadioStation(
        4,
        "Caracol Radio",
        "100.9 FM",
        "Noticias / Opinión / Deportes",
        "AAC+ • 64 kbps",
        true,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/CARACOL_RADIO_SC.mp3"
    ),
    RadioStation(
        5,
        "Olímpica Stereo",
        "105.9 FM",
        "Crossover / Vallenato / Salsa",
        "AAC+ • 128 kbps",
        true,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/OLIMPICA_BOG_SC.mp3"
    ),
    RadioStation(
        6,
        "Blu Radio",
        "89.9 FM",
        "Actualidad / Pop / Rock",
        "AAC+ • 128 kbps",
        false,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/BLU_RADIO_SC.mp3"
    ),
    RadioStation(
        7,
        "Tropicana",
        "102.9 FM",
        "Urbano / Salsa / Popular",
        "MP3 • 128 kbps",
        false,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/TROPICANA_BOG_SC.mp3"
    ),
    RadioStation(
        8,
        "La Mega",
        "90.9 FM",
        "Pop / Reggaeton / Juventud",
        "AAC+ • 96 kbps",
        false,
        "https://playerservices.streamtheworld.com/api/livestream-redirect/LA_MEGA_BOG_SC.mp3"
    ),
    RadioStation(
        9,
        "La X Más Música",
        "103.9 FM",
        "Rock / Pop / Electrónica",
        "AAC+ • 96 kbps",
        false,
        "https://stream.zeno.fm/0wr3c912g8uv"
    ),
    RadioStation(
        10,
        "IU Digital Radio",
        "98.5 FM",
        "Institucional & Crossover",
        "MP3 • 128 kbps",
        false,
        "https://stream.zeno.fm/f3wvbbqmdg8uv"
    )
)

val sampleGenres = listOf(
    GenreCategory("Pop", listOf(Color(0xFF8B263E), Color(0xFF4A1525))),
    GenreCategory("Rock", listOf(Color(0xFF6A35C9), Color(0xFF2C155E))),
    GenreCategory("Urbano", listOf(Color(0xFFD97706), Color(0xFFB45309))),
    GenreCategory("Noticias", listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
)

val genreToStationMap = mapOf(
    "Pop" to sampleStations[1],     // La FM
    "Rock" to sampleStations[8],    // La X
    "Urbano" to sampleStations[2],  // La Kalle
    "Noticias" to sampleStations[0] // W Radio
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedStation by remember { mutableStateOf(sampleStations[0]) }
    var isPlaying by rememberSaveable { mutableStateOf(false) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var selectedNavIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }

    val displayedStations = remember(selectedGenre) {
        if (selectedGenre == null) {
            sampleStations
        } else {
            sampleStations.filter { it.genre.contains(selectedGenre!!, ignoreCase = true) }
                .ifEmpty { sampleStations }
        }
    }

    // Initialize ExoPlayer with Cross-Protocol Redirects enabled for real live streams
    val exoPlayer = remember {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .setAllowCrossProtocolRedirects(true)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(selectedStation.streamUrl))
                prepare()
            }
    }

    LaunchedEffect(selectedStation) {
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(selectedStation.streamUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
    }

    LaunchedEffect(isPlaying, isMuted) {
        exoPlayer.playWhenReady = isPlaying
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            profileBitmap = bitmap
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    fun triggerHaptic() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        try {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(40)
                }
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color(0xFF1E3A8A),
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "IU Digital Radio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Tu música, tu momento",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .border(2.dp, Color(0xFF2563EB), CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E7FF))
                        .clickable {
                            triggerHaptic()
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Perfil",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf("Inicio", "Explorar", "En Vivo", "Perfil", "Ajustes")
                val icons = listOf<ImageVector>(
                    Icons.Default.Home,
                    Icons.Default.Explore,
                    Icons.Default.Radio,
                    Icons.Default.Person,
                    Icons.Default.Settings
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item, fontSize = 10.sp) },
                        selected = selectedNavIndex == index,
                        onClick = {
                            triggerHaptic()
                            selectedNavIndex = index
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF312E81),
                                    Color(0xFFC2410C)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Colombia • ${selectedStation.genre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = selectedStation.bitrate,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(35.dp)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "player_eq")
                            repeat(18) { index ->
                                val height by infiniteTransition.animateFloat(
                                    initialValue = 8f,
                                    targetValue = if (isPlaying && !isMuted) (10f + ((index * 7) % 24f)) else 8f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(250 + (index * 40), easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "bar_$index"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(height.dp)
                                        .background(
                                            color = if (isPlaying && !isMuted) Color(0xFF38BDF8) else Color.Gray,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPlaying) "Reproduciendo en vivo" else "En pausa",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Sonando: ${selectedStation.name} - ${selectedStation.frequency}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clickable {
                                            triggerHaptic()
                                            isMuted = !isMuted
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "Mute",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Text(
                                    "Mute",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clickable {
                                            triggerHaptic()
                                            isPlaying = true
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Play",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clickable {
                                            triggerHaptic()
                                            isPlaying = false
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Pause,
                                            contentDescription = "Pause",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Text(
                                    "Pause",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Explora por tipo de radio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = if (selectedGenre != null) "Filtrado por: $selectedGenre (Toca de nuevo para ver todas)" else "Cada género tiene su propio estilo",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0284C7)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(sampleGenres) { genre ->
                        val isGenreSelected = selectedGenre == genre.name
                        Card(
                            modifier = Modifier
                                .size(width = 130.dp, height = 90.dp)
                                .clickable {
                                    triggerHaptic()
                                    if (isGenreSelected) {
                                        selectedGenre = null
                                    } else {
                                        selectedGenre = genre.name
                                        genreToStationMap[genre.name]?.let { station ->
                                            selectedStation = station
                                            isPlaying = true
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isGenreSelected) 8.dp else 4.dp),
                            border = if (isGenreSelected) BorderStroke(2.dp, Color.White) else null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(genre.colors))
                                    .padding(12.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedGenre != null) "Emisoras: $selectedGenre" else "Emisoras Disponibles (${displayedStations.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    if (selectedGenre != null) {
                        TextButton(onClick = { selectedGenre = null }) {
                            Text("Ver todas", fontSize = 12.sp)
                        }
                    }
                }

                displayedStations.forEach { station ->
                    val isSelected = station.id == selectedStation.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                triggerHaptic()
                                selectedStation = station
                                isPlaying = true
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = if (isSelected) BorderStroke(1.dp, Color(0xFF2563EB)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(32.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = station.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (station.isOfficial) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF2563EB)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Emisora oficial",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = station.bitrate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    triggerHaptic()
                                    selectedStation = station
                                    isPlaying = !isPlaying
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFEFF6FF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isSelected && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color(0xFF2563EB)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
