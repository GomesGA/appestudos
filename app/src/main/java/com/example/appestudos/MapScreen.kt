package com.example.appestudos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.*

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Inicializar Places API
    val placesClient = remember {
        try {
            if (!Places.isInitialized()) {
                Places.initialize(context, context.getString(R.string.google_maps_key))
                println("Places API inicializado com sucesso")
            }
            Places.createClient(context).also {
                println("Places Client criado com sucesso")
            }
        } catch (e: Exception) {
            println("Erro ao inicializar Places API: ${e.message}")
            null
        }
    }
    
    // Estado para armazenar a localização atual
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    
    // Estado para controlar se o mapa deve seguir a localização do usuário
    var isFollowingUser by remember { mutableStateOf(true) }
    
    // Estado para permissões
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Inicializar o FusedLocationProviderClient
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Configuração para atualizações de localização
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(10f)
            .build()
    }

    // Callback para atualizações de localização
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    currentLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    // Launcher para solicitar permissão
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            startLocationUpdates(fusedLocationClient, locationRequest, locationCallback)
        }
    }

    // Solicitar permissão e iniciar atualizações de localização
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            startLocationUpdates(fusedLocationClient, locationRequest, locationCallback)
        }
    }

    // Limpar o callback quando o composable for destruído
    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false // Desativamos o botão padrão para usar nosso próprio
            )
        )
    }

    val properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        )
    }

    // Usar a localização atual ou uma localização padrão
    val defaultLocation = LatLng(-18.913664, -48.266560) // Uberlândia como fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: defaultLocation,
            15f
        )
    }

    // Atualizar a posição da câmera quando a localização mudar e estiver seguindo o usuário
    LaunchedEffect(currentLocation) {
        if (isFollowingUser) {
            currentLocation?.let { location ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(location)
                            .zoom(15f)
                            .build()
                    ),
                    durationMs = 1000
                )
            }
        }
    }

    // Função para realizar a pesquisa
    val performSearch = { query: String ->
        if (query.isNotEmpty() && placesClient != null) {
            isSearching = true
            errorMessage = null
            println("Iniciando pesquisa para: $query")

            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setCountries("BR")
                    .build()

                placesClient?.findAutocompletePredictions(request)
                    ?.addOnSuccessListener { response ->
                        val predictions = response.autocompletePredictions
                        println("Sugestões recebidas: ${predictions.size}")
                        predictions.forEach { prediction ->
                            println("Sugestão: ${prediction.getFullText(null)}")
                        }
                        searchResults = predictions
                        isSearching = false
                    }
                    ?.addOnFailureListener { exception ->
                        println("Erro na busca: ${exception.message}")
                        println("Stack trace: ${exception.stackTraceToString()}")
                        errorMessage = "Erro na busca: ${exception.message}"
                        searchResults = emptyList()
                        isSearching = false
                    }
            } catch (e: Exception) {
                println("Exceção ao fazer a busca: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
                errorMessage = "Erro: ${e.message}"
                searchResults = emptyList()
                isSearching = false
            }
        } else {
            if (placesClient == null) {
                println("Places Client não está inicializado")
                errorMessage = "Serviço de busca não está disponível"
            }
            searchResults = emptyList()
            isSearching = false
        }
    }

    // Função para selecionar um lugar
    val selectPlace = { prediction: AutocompletePrediction ->
        scope.launch {
            val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
            val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

            placesClient?.fetchPlace(request)
                ?.addOnSuccessListener { response ->
                    response.place.latLng?.let { latLng ->
                        searchQuery = prediction.getPrimaryText(null).toString()
                        searchResults = emptyList()
                        isFollowingUser = false
                        
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.Builder()
                                        .target(latLng)
                                        .zoom(15f)
                                        .build()
                                ),
                                durationMs = 1000
                            )
                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    println("Erro ao buscar detalhes do lugar: ${exception.message}")
                    errorMessage = "Erro ao buscar detalhes do lugar"
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapClick = { isFollowingUser = false }
        )

        // Search bar and results
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(1f)
        ) {
            // Search Bar with back button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                elevation = 4.dp,
                backgroundColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.Red
                        )
                    }

                    TextField(
                        value = searchQuery,
                        onValueChange = { newValue -> 
                            searchQuery = newValue
                            performSearch(newValue)
                        },
                        placeholder = { Text("Cidade ou Bairro", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black,
                            textColor = Color.Black
                        ),
                        singleLine = true
                    )

                    // Clear button
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                searchQuery = ""
                                searchResults = emptyList()
                                errorMessage = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Search Results
            if (searchResults.isNotEmpty()) {
                println("Mostrando ${searchResults.size} sugestões")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    elevation = 4.dp,
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(searchResults) { prediction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        println("Clicou em: ${prediction.getPrimaryText(null)}")
                                        selectPlace(prediction)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = prediction.getPrimaryText(null).toString(),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = prediction.getSecondaryText(null).toString(),
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            if (searchResults.last() != prediction) {
                                Divider(
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Loading indicator
            if (isSearching) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = Color.Red
                )
            }

            // Filter and Sort buttons (only show when not searching)
            if (!isSearching && searchResults.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Implement filter */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Filtro", color = Color.Black)
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = Color.Black
                            )
                        }
                    }

                    Button(
                        onClick = { /* TODO: Implement sort */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        elevation = ButtonDefaults.elevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ordem", color = Color.Black)
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Recenter button
        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    isFollowingUser = true
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(location)
                                    .zoom(15f)
                                    .build()
                            ),
                            durationMs = 1000
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centralizar no usuário",
                tint = if (isFollowingUser) Color.Blue else Color.Black
            )
        }
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    locationRequest: LocationRequest,
    locationCallback: LocationCallback
) {
    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
} 