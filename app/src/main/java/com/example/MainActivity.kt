package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NexusMapsApp()
            }
        }
    }
}

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun NexusMapsApp() {
    val viewModel: com.example.viewmodel.MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val centerLat by viewModel.centerLat.collectAsState()
    val centerLon by viewModel.centerLon.collectAsState()
    val is3DMode by viewModel.is3DMode.collectAsState()
    val layersVisible by viewModel.layersVisible.collectAsState()

    var isSplitView by remember { mutableStateOf(true) }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }
    
    val locationPermissionState = com.google.accompanist.permissions.rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    LaunchedEffect(Unit) {
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    val locationEnabled = locationPermissionState.allPermissionsGranted
    val mapProvider = remember { com.example.providers.OSMDroidMapProvider() }
    
    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        // Map Background
        if (isSplitView) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxHeight().weight(splitFraction)) {
                    mapProvider.MapLayer(
                        modifier = Modifier, 
                        satellite = true, 
                        locationEnabled = locationEnabled,
                        zoomLevel = zoomLevel,
                        centerLat = centerLat,
                        centerLon = centerLon
                    )
                }
                Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(DividerColor))
                Box(modifier = Modifier.fillMaxHeight().weight(1f - splitFraction)) {
                    mapProvider.MapLayer(
                        modifier = Modifier, 
                        satellite = false, 
                        locationEnabled = locationEnabled,
                        zoomLevel = zoomLevel,
                        centerLat = centerLat,
                        centerLon = centerLon
                    )
                }
            }
            
            // Split view divider thumb
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            Box(
                modifier = Modifier
                    .offset { IntOffset((screenWidth.toPx() * splitFraction).roundToInt() - 24, 0) }
                    .align(Alignment.CenterStart)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            val newFraction = splitFraction + (delta / (screenWidth.value * 2.5f))
                            splitFraction = newFraction.coerceIn(0.1f, 0.9f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = "Drag to resize", tint = Color.White)
            }
        } else {
            mapProvider.MapLayer(
                modifier = Modifier, 
                satellite = false, 
                locationEnabled = locationEnabled,
                zoomLevel = zoomLevel,
                centerLat = centerLat,
                centerLon = centerLon
            )
        }
        
        // UI Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {
            TopHeader()
            
            // Map Mode labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MapModeChip(title = "SATELLITE", subtitle = "Ultra Realistic", color = CyanHighlight, icon = Icons.Outlined.Layers)
                MapModeChip(title = "GAME / NORMAL", subtitle = "Immersive 3D", color = PurpleHighlight, icon = Icons.Outlined.SportsEsports)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Map Scale Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        modifier = Modifier.width(150.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", color = TextPrimary, fontSize = 10.sp)
                        Text("250 m", color = TextPrimary, fontSize = 10.sp)
                        Text("500 m", color = TextPrimary, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.width(150.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(1.dp).height(6.dp).background(TextPrimary))
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(TextPrimary))
                        Box(modifier = Modifier.width(1.dp).height(6.dp).background(TextPrimary))
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(TextPrimary))
                        Box(modifier = Modifier.width(1.dp).height(6.dp).background(TextPrimary))
                    }
                }
            }
            
            // Floating controls left/right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left controls
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingMapButton(
                        text = if (is3DMode) "2D" else "3D",
                        onClick = { viewModel.toggle3DMode() }
                    )
                    FloatingMapButton(
                        icon = if (layersVisible) Icons.Outlined.Layers else Icons.Outlined.LayersClear,
                        onClick = { viewModel.toggleLayers() }
                    )
                    FloatingMapButton(icon = Icons.Outlined.GpsFixed)
                    WeatherWidget()
                }
                
                // Right controls
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingMapButton(icon = Icons.Outlined.Explore) // Compass
                    FloatingMapButton(
                        icon = Icons.Outlined.MyLocation,
                        onClick = { 
                            if (locationEnabled) {
                                viewModel.setCenter(28.6139, 77.2090)
                            }
                        }
                    )
                    FloatingMapButton(icon = Icons.Outlined.DirectionsWalk) // Street view
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            BottomActionPanel()
        }
    }
}

@Composable
fun TopHeader() {
    var searchQuery by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val searchResults = listOf("Pacific Mall", "India Gate", "Delhi airport", "restaurants near me")

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, contentDescription = "Logo", tint = CyanHighlight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MAPAURAGOEX", color = TextPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Text("Explore. Navigate. Discover.", color = TextSecondary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            AsyncImage(
                model = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100&q=80",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, PurpleHighlight, CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark.copy(alpha = 0.8f))
                .border(1.dp, DividerColor, RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it 
                        isDropdownExpanded = it.isNotEmpty()
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 14.sp),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search any place, address, business...", color = TextSecondary, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = "Clear", 
                        tint = TextSecondary,
                        modifier = Modifier.clickable { 
                            searchQuery = "" 
                            isDropdownExpanded = false
                        }
                    )
                } else {
                    Icon(Icons.Default.Mic, contentDescription = "Voice", tint = TextSecondary)
                }
            }
            
            // Dropdown list
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(SurfaceDark)
                    .border(1.dp, DividerColor, RoundedCornerShape(8.dp))
            ) {
                val filtered = searchResults.filter { it.contains(searchQuery, ignoreCase = true) }
                if (filtered.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No results found", color = TextSecondary) },
                        onClick = { }
                    )
                } else {
                    filtered.forEach { result ->
                        DropdownMenuItem(
                            text = { Text(result, color = TextPrimary) },
                            onClick = {
                                searchQuery = result
                                isDropdownExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = TextSecondary)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MapModeChip(title: String, subtitle: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(alpha = 0.8f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun FloatingMapButton(icon: ImageVector? = null, text: String? = null, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark.copy(alpha = 0.8f))
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = TextPrimary)
        } else if (text != null) {
            Text(text, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeatherWidget() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark.copy(alpha = 0.8f))
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.WbSunny, contentDescription = "Weather", tint = Color(0xFFFFC107), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text("28°C", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Sunny", color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun BottomActionPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(SurfaceDark)
            .border(1.dp, DividerColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DividerColor)
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionItem(icon = Icons.Outlined.Directions, label = "Directions", tint = CyanHighlight)
            ActionItem(icon = Icons.Outlined.BookmarkBorder, label = "Save", tint = CyanHighlight)
            ActionItem(icon = Icons.Outlined.LocationOn, label = "Nearby", tint = CyanHighlight)
            ActionItem(icon = Icons.Outlined.Train, label = "Transit", tint = PurpleHighlight)
            ActionItem(icon = Icons.Outlined.Straighten, label = "Measure", tint = Color(0xFFFFC107))
            ActionItem(icon = Icons.Outlined.GridView, label = "More", tint = TextPrimary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Place Card
        PlaceCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bottom Navigation
        BottomNav()
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepNavy)
                .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextPrimary, fontSize = 10.sp)
    }
}

@Composable
fun PlaceCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepNavy)
            .padding(12.dp)
    ) {
        Row {
            // Main place image
            AsyncImage(
                model = "https://images.unsplash.com/photo-1519567241046-7f152d5bfb91?w=400&q=80",
                contentDescription = "Place image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Pacific Mall", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Shopping Mall • 5.2 km", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("4.3 ", color = Color(0xFFFFC107), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row {
                        repeat(4) { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp)) }
                        Icon(Icons.Default.StarHalf, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                    }
                    Text(" (12,543)", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text("Open", color = Color(0xFF4CAF50), fontSize = 12.sp)
                    Text(" • Closes 10:00 PM", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Image Carousel
        Row(verticalAlignment = Alignment.CenterVertically) {
            
            val images = listOf(
                "https://images.unsplash.com/photo-1541890289-b86df5baff04?w=400&q=80",
                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&q=80",
                "https://images.unsplash.com/photo-1565551227-d4a13b6bf434?w=400&q=80"
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(images.size) { index ->
                    val url = images[index]
                    AsyncImage(
                        model = url,
                        contentDescription = "Additional image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(100.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = "More images", tint = TextPrimary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun BottomNav() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(icon = Icons.Outlined.Language, label = "Explore", selected = true, color = CyanHighlight)
        BottomNavItem(icon = Icons.Outlined.Navigation, label = "Navigate")
        
        // Center FAB
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(2.dp, CyanHighlight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Navigation, contentDescription = "Center", tint = CyanHighlight, modifier = Modifier.size(32.dp))
        }
        
        BottomNavItem(icon = Icons.Outlined.BookmarkBorder, label = "Saved")
        BottomNavItem(icon = Icons.Outlined.PeopleOutline, label = "Contribute")
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, selected: Boolean = false, color: Color = TextSecondary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = if (selected) color else TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = if (selected) color else TextSecondary, fontSize = 10.sp)
    }
}
