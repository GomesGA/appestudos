package com.example.appestudos.features.map.ui

import androidx.compose.ui.res.colorResource
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.appestudos.features.map.data.FavoriteLocationDatabase
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.*
import kotlinx.coroutines.*
import androidx.compose.ui.platform.LocalFocusManager
import com.example.appestudos.R
import com.example.appestudos.features.auth.data.UserManager
import com.example.appestudos.ui.theme.LocalThemeManager
import com.google.android.gms.maps.model.MapStyleOptions

data class FavoriteLocation(
    val id: Int,
    val name: String,
    val location: LatLng
)

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val themeManager = LocalThemeManager.current
    val isDark = themeManager.isDarkMode
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLocationLoaded by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val darkMapStyleJson = remember {
        runCatching {
            context.resources
                .openRawResource(R.raw.map_style_dark)
                .bufferedReader()
                .use { it.readText() }
        }.getOrNull()
    }

    // Estado para permissões
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val properties by remember(isDark, darkMapStyleJson) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = if (isDark && darkMapStyleJson != null)
                    MapStyleOptions(darkMapStyleJson)
                else null
            )
        )
    }

    // Substituir FocusRequester por LocalFocusManager
    val focusManager = LocalFocusManager.current

    // Inicializar o banco de dados
    val database = remember { FavoriteLocationDatabase(context) }

    // Estados para localizações favoritas
    var favoriteLocations by remember { mutableStateOf<List<FavoriteLocation>>(emptyList()) }
    var showFavoritesPanel by remember { mutableStateOf(false) }

    // Adicionar estados para edição
    var editingLocation by remember { mutableStateOf<FavoriteLocation?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf("") }
    var isEditingLocation by remember { mutableStateOf(false) }

    // Adicionar estados para pesquisa na edição
    var editSearchQuery by remember { mutableStateOf("") }
    var editSearchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isEditSearching by remember { mutableStateOf(false) }
    var editErrorMessage by remember { mutableStateOf<String?>(null) }
    // Usar a localização atual ou uma localização padrão
    val defaultLocation = LatLng(-18.913664, -48.266560)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // Carregar localizações favoritas do banco de dados
    LaunchedEffect(Unit) {
        UserManager.getCurrentUserId()?.let { userId ->
            favoriteLocations = database.getAllFavoriteLocations(userId)
        }
    }

    // Inicializar Places API
    val placesClient = remember {
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val apiKey = applicationInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""

            if (!Places.isInitialized()) {
                Places.initialize(context, apiKey)
                println("Places API inicializado com sucesso")
            }
            Places.createClient(context).also {
                println("Places Client criado com sucesso")
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Erro detalhado ao inicializar Places API", e)
            println("Erro ao inicializar Places API: ${e.message}")
            null
        }
    }

    // Estado para armazenar a localização atual
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para controlar se o mapa deve seguir a localização do usuário
    var isFollowingUser by remember { mutableStateOf(true) }

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
            // user accepted → start GPS
            startLocationUpdates(fusedLocationClient, locationRequest, locationCallback)
        } else {
            // user denied → show the map at default and mark loaded
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(defaultLocation, 15f)
            isLocationLoaded = true
        }
    }

    // Solicitar permissão e iniciar atualizações de localização
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            startLocationUpdates(fusedLocationClient, locationRequest, locationCallback)
        } else if (!permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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

    // Atualizar a posição da câmera quando a localização mudar e estiver seguindo o usuário
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 15f)
            isLocationLoaded = true
        }
    }

    // Função para realizar a pesquisa
    fun performSearch(query: String, onResult: (List<AutocompletePrediction>, String?) -> Unit) {
        if (query.isNotEmpty() && placesClient != null) {
            try {
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setCountries("BR")
                    .build()

                placesClient.findAutocompletePredictions(request)
                    ?.addOnSuccessListener { response ->
                        onResult(response.autocompletePredictions, null)
                    }
                    ?.addOnFailureListener { exception ->
                        onResult(emptyList(), "Erro na busca: ${exception.message}")
                    }
            } catch (e: Exception) {
                onResult(emptyList(), "Erro: ${e.message}")
            }
        } else {
            if (placesClient == null) {
                onResult(emptyList(), "Serviço de busca não está disponível")
            } else {
                onResult(emptyList(), null)
            }
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

    // Função para adicionar localização favorita
    fun addFavoriteLocation(name: String, location: LatLng) {
        UserManager.getCurrentUserId()?.let { userId ->
            database.addFavoriteLocation(userId, name, location)
            favoriteLocations = database.getAllFavoriteLocations(userId)
        } ?: run {
            Toast.makeText(context, "Você precisa estar logado para adicionar localizações favoritas", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para remover localização favorita
    fun removeFavoriteLocation(id: Int) {
        UserManager.getCurrentUserId()?.let { userId ->
            database.deleteFavoriteLocation(id)
            favoriteLocations = database.getAllFavoriteLocations(userId)
        }
    }

    // Função para atualizar localização favorita
    fun updateFavoriteLocation(id: Int, name: String, location: LatLng) {
        UserManager.getCurrentUserId()?.let { userId ->
            database.updateFavoriteLocation(id, name, location)
            favoriteLocations = database.getAllFavoriteLocations(userId)
        }
    }

    // Função para limpar a pesquisa
    val clearSearch = {
        searchQuery = ""
        searchResults = emptyList()
        errorMessage = null
        isSearching = false
        focusManager.clearFocus()
    }

    Box(modifier = Modifier.fillMaxSize().background(if (isDark) Color(0xFF121212) else Color.White)) {
        if (isLocationLoaded) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { clearSearch() },
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = uiSettings,
                onMapClick = { latLng ->
                    clearSearch()
                    isFollowingUser = false
                    if (isEditingLocation && editingLocation != null) {
                        updateFavoriteLocation(
                            editingLocation!!.id,
                            editingName,
                            latLng
                        )
                        isEditingLocation = false
                        editingLocation = null
                        editingName = ""
                        showEditDialog = false
                    }
                }
            ) {
                // Mostrar marcadores para localizações favoritas
                favoriteLocations.forEach { favorite ->
                    Marker(
                        state = MarkerState(position = favorite.location),
                        title = favorite.name,
                        snippet = "Localização favorita",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (isDark) Color.White else Color.Red)
            }
        }

        // Search bar and results
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .zIndex(1f)
        ) {
            // Search Bar with back button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                elevation = 4.dp,
                backgroundColor = if (isDark) Color(0xFF222222) else Color.White,
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }

                    TextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue

                            if (newValue.isNotBlank()) {
                                isSearching = true
                                performSearch(newValue) { predictions, error ->
                                    searchResults = predictions
                                    errorMessage = error
                                    isSearching = false
                                }
                            } else {
                                // Se esvaziou o campo, limpa tudo e desliga o loading
                                searchResults = emptyList()
                                errorMessage = null
                                isSearching = false
                            }
                        },
                        placeholder = { Text("Cidade ou Bairro", color = if (isDark) Color.LightGray else Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = if (isDark) Color(0xFF222222) else Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = if (isDark) Color.White else Color.Black,
                            cursorColor = if (isDark) Color.White else Color.Black
                        ),
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        searchResults = emptyList()
                                        errorMessage = null
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpar",
                                        tint = if (isDark) Color.LightGray else Color.Gray
                                    )
                                }
                            }
                        } else null
                    )
                }
            }

            // Search results
            if (searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    elevation = 4.dp,
                    backgroundColor = if (isDark) Color(0xFF222222) else Color.White
                ) {
                    LazyColumn {
                        items(searchResults) { prediction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectPlace(prediction) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = prediction.getPrimaryText(null).toString(),
                                        style = MaterialTheme.typography.subtitle1,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Text(
                                        text = prediction.getSecondaryText(null).toString(),
                                        style = MaterialTheme.typography.caption,
                                        color = if (isDark) Color.LightGray else Color.Gray
                                    )
                                }
                                // Botão de adicionar aos favoritos (MANTIDO)
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                                            val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

                                            placesClient?.fetchPlace(request)
                                                ?.addOnSuccessListener { response ->
                                                    response.place.latLng?.let { latLng ->
                                                        UserManager.getCurrentUserId()?.let { userId ->
                                                            if (database.getFavoriteLocationsCount(userId) < 7) {
                                                                addFavoriteLocation(
                                                                    prediction.getPrimaryText(null).toString(),
                                                                    latLng
                                                                )
                                                                Toast.makeText(
                                                                    context,
                                                                    "Localização adicionada aos favoritos",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Limite de 7 localizações favoritas atingido",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        } ?: run {
                                                            Toast.makeText(
                                                                context,
                                                                "Você precisa estar logado para adicionar localizações favoritas",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Adicionar aos favoritos",
                                        tint = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                            if (searchResults.last() != prediction) {
                                Divider(
                                    color = if (isDark) Color.DarkGray else Color.LightGray,
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
                    color = if (isDark) Color.Red else Color.Red
                )
            }
        }

        // Recenter button
        FloatingActionButton(
            onClick = {
                clearSearch()
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
                .padding(end = 16.dp, bottom = 80.dp),
            backgroundColor = if (isDark) Color.Black else Color.White
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centralizar no usuário",
                tint = if (isFollowingUser) Color(0xFF1E90FF) else if (isDark) Color.White else Color.Black
            )
        }

        // Botão para mostrar localizações favoritas
        FloatingActionButton(
            onClick = {
                clearSearch()
                showFavoritesPanel = !showFavoritesPanel
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 80.dp),
            backgroundColor = if (isDark) Color.Black else Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Localizações favoritas",
                tint = if (showFavoritesPanel) Color(0xFF1E90FF) else if (isDark) Color.White else Color.Black
            )
        }

        // Painel de localizações favoritas
        if (showFavoritesPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showFavoritesPanel = false }
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                        .width(300.dp)
                        .heightIn(max = 400.dp)
                        .clickable(enabled = false) { }, // Previne que o clique no card feche o painel
                    elevation = 8.dp,
                    backgroundColor = if (isDark) Color(0xFF222222) else Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Localizações Favoritas",
                                style = MaterialTheme.typography.h6,
                                color = if (isDark) Color.White else Color.Black
                            )
                            IconButton(onClick = { showFavoritesPanel = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = if (isDark) Color.White else Color.Gray
                                )
                            }
                        }

                        if (favoriteLocations.isEmpty()) {
                            Text(
                                text = "Nenhuma localização favorita adicionada",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyColumn {
                                items(favoriteLocations) { favorite ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = favorite.name,
                                                style = MaterialTheme.typography.subtitle1,
                                                color = if (isDark) Color.White else Color.Black
                                            )
                                            Text(
                                                text = "Lat: ${favorite.location.latitude}, Lng: ${favorite.location.longitude}",
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                            )
                                        }

                                        // Botão de editar
                                        IconButton(
                                            onClick = {
                                                editingLocation = favorite
                                                editingName = favorite.name
                                                showEditDialog = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar favorito",
                                                tint = colorResource(id = R.color.blue_p)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    cameraPositionState.animate(
                                                        update = CameraUpdateFactory.newCameraPosition(
                                                            CameraPosition.Builder()
                                                                .target(favorite.location)
                                                                .zoom(15f)
                                                                .build()
                                                        ),
                                                        durationMs = 1000
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Ir para localização",
                                                tint = colorResource(id = R.color.blue_p)
                                            )
                                        }

                                        IconButton(
                                            onClick = { removeFavoriteLocation(favorite.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remover favorito",
                                                tint = colorResource(id = R.color.red_p)
                                            )
                                        }
                                    }
                                    if (favorite != favoriteLocations.last()) {
                                        Divider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de edição
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = {
                    showEditDialog = false
                    editingLocation = null
                    editingName = ""
                    isEditingLocation = false
                    editSearchQuery = ""
                    editSearchResults = emptyList()
                },
                title = { Text("Editar localização favorita") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = editingName,
                            onValueChange = { editingName = it },
                            label = { Text("Nome da localização") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de pesquisa
                        TextField(
                            value = editSearchQuery,
                            onValueChange = { newValue ->
                                editSearchQuery = newValue
                                performSearch(newValue) { predictions, error ->
                                    editSearchResults = predictions
                                    editErrorMessage = error
                                    isEditSearching = false
                                }
                                isEditSearching = true
                            },
                            placeholder = { Text("Pesquisar novo endereço") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = if (editSearchQuery.isNotEmpty()) {
                                {
                                    IconButton(
                                        onClick = {
                                            editSearchQuery = ""
                                            editSearchResults = emptyList()
                                            editErrorMessage = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpar",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            } else null
                        )

                        // Indicador de carregamento
                        if (isEditSearching) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                color = Color.Red
                            )
                        }

                        // Mensagem de erro
                        editErrorMessage?.let {
                            Text(
                                text = it,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Resultados da pesquisa
                        if (editSearchResults.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                            ) {
                                items(editSearchResults) { prediction ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                scope.launch {
                                                    val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                                                    val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

                                                    placesClient?.fetchPlace(request)
                                                        ?.addOnSuccessListener { response ->
                                                            response.place.latLng?.let { latLng ->
                                                                editingLocation?.let { location ->
                                                                    updateFavoriteLocation(
                                                                        location.id,
                                                                        editingName,
                                                                        latLng
                                                                    )
                                                                }
                                                                showEditDialog = false
                                                                editingLocation = null
                                                                editingName = ""
                                                                editSearchQuery = ""
                                                                editSearchResults = emptyList()
                                                            }
                                                        }
                                                }
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = prediction.getPrimaryText(null).toString(),
                                                style = MaterialTheme.typography.subtitle1
                                            )
                                            Text(
                                                text = prediction.getSecondaryText(null).toString(),
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    if (prediction != editSearchResults.last()) {
                                        Divider(
                                            color = Color.LightGray,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isEditingLocation = true
                                showEditDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(android.graphics.Color.parseColor("#1E90FF")))
                        ) {
                            Text(
                                text = "Selecionar no mapa",
                                color = Color.White
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            editingLocation?.let { location ->
                                updateFavoriteLocation(
                                    location.id,
                                    editingName,
                                    location.location
                                )
                            }
                            showEditDialog = false
                            editingLocation = null
                            editingName = ""
                        }
                    ) {
                        Text(
                            text = "Salvar",
                            color = colorResource(id = R.color.blue_p)

                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            editingLocation = null
                            editingName = ""
                            isEditingLocation = false
                        }
                    ) {
                        Text(
                            "Cancelar",
                            color = colorResource(id = R.color.blue_p)
                        )
                    }
                }
            )
        }

        // Indicador de edição de localização
        if (isEditingLocation) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Text(
                    text = "Selecione a nova localização no mapa",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun startLocationUpdates(
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
    } catch (e: Exception) {
        println("Erro ao iniciar atualizações de localização: ${e.message}")
    }
}