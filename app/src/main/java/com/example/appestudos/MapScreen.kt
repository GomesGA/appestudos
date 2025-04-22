package com.example.appestudos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
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
import com.example.appestudos.database.FavoriteLocationDatabase
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

data class FavoriteLocation(
    val id: Int,
    val name: String,
    val location: LatLng
)

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Inicializar o banco de dados
    val database = remember { FavoriteLocationDatabase(context) }
    
    // Estados para localizações favoritas
    var favoriteLocations by remember { mutableStateOf<List<FavoriteLocation>>(emptyList()) }
    var showAddFavoriteDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var newFavoriteName by remember { mutableStateOf("") }
    var showFavoritesPanel by remember { mutableStateOf(false) }
    var isSelectingLocation by remember { mutableStateOf(false) }

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

    // Carregar localizações favoritas do banco de dados
    LaunchedEffect(Unit) {
        favoriteLocations = database.getAllFavoriteLocations()
    }

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

    // Função auxiliar para realizar pesquisa
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
    val addFavoriteLocation = { location: LatLng, name: String ->
        if (database.getFavoriteLocationsCount() < 7) {
            val id = database.addFavoriteLocation(name, location)
            if (id != -1L) {
                val newFavorite = FavoriteLocation(
                    id = id.toInt(),
                    name = name,
                    location = location
                )
                favoriteLocations = favoriteLocations + newFavorite
                showAddFavoriteDialog = false
                newFavoriteName = ""
            }
        }
    }

    // Função para remover localização favorita
    val removeFavoriteLocation = { id: Int ->
        database.deleteFavoriteLocation(id)
        favoriteLocations = favoriteLocations.filter { it.id != id }
    }

    // Função para atualizar localização favorita
    val updateFavoriteLocation = { id: Int, name: String, location: LatLng ->
        database.updateFavoriteLocation(id, name, location)
        favoriteLocations = favoriteLocations.map { 
            if (it.id == id) it.copy(name = name, location = location) else it 
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            onMapClick = { latLng -> 
                isFollowingUser = false
                if (isSelectingLocation) {
                    selectedLocation = latLng
                    showAddFavoriteDialog = true
                    isSelectingLocation = false
                } else if (isEditingLocation && editingLocation != null) {
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
                            performSearch(newValue) { predictions, error ->
                                searchResults = predictions
                                errorMessage = error
                                isSearching = false
                            }
                            isSearching = true
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
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
                                
                                // Botão de adicionar aos favoritos
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
                                            val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

                                            placesClient?.fetchPlace(request)
                                                ?.addOnSuccessListener { response ->
                                                    response.place.latLng?.let { latLng ->
                                                        if (database.getFavoriteLocationsCount() < 7) {
                                                            val id = database.addFavoriteLocation(
                                                                prediction.getPrimaryText(null).toString(),
                                                                latLng
                                                            )
                                                            if (id != -1L) {
                                                                val newFavorite = FavoriteLocation(
                                                                    id = id.toInt(),
                                                                    name = prediction.getPrimaryText(null).toString(),
                                                                    location = latLng
                                                                )
                                                                favoriteLocations = favoriteLocations + newFavorite
                                                                Toast.makeText(
                                                                    context,
                                                                    "Localização adicionada aos favoritos",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Limite de 7 localizações favoritas atingido",
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
                                        tint = Color.Black
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

        // Botão para adicionar nova localização favorita
        FloatingActionButton(
            onClick = { 
                if (favoriteLocations.size >= 7) {
                    // Mostrar mensagem de limite atingido
                    Toast.makeText(context, "Limite de 7 localizações favoritas atingido", Toast.LENGTH_SHORT).show()
                } else {
                    showAddFavoriteDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            backgroundColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar localização favorita",
                tint = Color.Black
            )
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

        // Botão para mostrar localizações favoritas
        FloatingActionButton(
            onClick = { showFavoritesPanel = !showFavoritesPanel },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            backgroundColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Localizações favoritas",
                tint = if (showFavoritesPanel) Color.Blue else Color.Black
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
                        .padding(16.dp)
                        .width(300.dp)
                        .heightIn(max = 400.dp)
                        .clickable(enabled = false) { }, // Previne que o clique no card feche o painel
                    elevation = 8.dp,
                    backgroundColor = Color.White
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
                                style = MaterialTheme.typography.h6
                            )
                            IconButton(onClick = { showFavoritesPanel = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = Color.Gray
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
                                                style = MaterialTheme.typography.subtitle1
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
                                                tint = Color.Blue
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
                                                tint = Color.Blue
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = { removeFavoriteLocation(favorite.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remover favorito",
                                                tint = Color.Red
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

        // Diálogo para adicionar localização favorita
        if (showAddFavoriteDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddFavoriteDialog = false
                    newFavoriteName = ""
                    selectedLocation = null
                    isSelectingLocation = false
                },
                title = { Text("Adicionar localização favorita") },
                text = {
                    Column {
                        if (selectedLocation == null) {
                            TextField(
                                value = newFavoriteName,
                                onValueChange = { newFavoriteName = it },
                                label = { Text("Nome da localização") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Clique em 'Continuar' e selecione a localização no mapa",
                                color = Color.Gray,
                                style = MaterialTheme.typography.caption
                            )
                        } else {
                            Text(
                                text = "Nome: $newFavoriteName",
                                style = MaterialTheme.typography.subtitle1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Localização selecionada:",
                                style = MaterialTheme.typography.subtitle2
                            )
                            Text(
                                text = "Lat: ${selectedLocation?.latitude}, Lng: ${selectedLocation?.longitude}",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                            )
                        }
                    }
                },
                confirmButton = {
                    if (selectedLocation == null) {
                        TextButton(
                            onClick = {
                                if (newFavoriteName.isNotBlank()) {
                                    isSelectingLocation = true
                                    showAddFavoriteDialog = false
                                }
                            }
                        ) {
                            Text("Continuar")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                selectedLocation?.let { location ->
                                    addFavoriteLocation(location, newFavoriteName)
                                }
                            }
                        ) {
                            Text("Adicionar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showAddFavoriteDialog = false
                            newFavoriteName = ""
                            selectedLocation = null
                            isSelectingLocation = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Indicador de seleção de localização
        if (isSelectingLocation) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Text(
                    text = "Selecione a localização no mapa",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
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
                                    if (prediction != editSearchResults.last()) {
                                        Divider(
                                            color = Color.LightGray,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Ou selecionar no mapa
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { isEditingLocation = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ou selecione um local no mapa")
                        }

                        if (isEditingLocation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Clique no mapa para selecionar a nova localização",
                                color = Color.Gray
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            editingLocation?.let { location ->
                                if (editingName.isNotBlank() && !isEditingLocation) {
                                    updateFavoriteLocation(
                                        location.id,
                                        editingName,
                                        location.location
                                    )
                                    showEditDialog = false
                                    editingLocation = null
                                    editingName = ""
                                    editSearchQuery = ""
                                    editSearchResults = emptyList()
                                } else {
                                    showEditDialog = false
                                }
                            }
                        }
                    ) {
                        Text(if (!isEditingLocation) "Salvar" else "Continuar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showEditDialog = false
                            editingLocation = null
                            editingName = ""
                            isEditingLocation = false
                            editSearchQuery = ""
                            editSearchResults = emptyList()
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
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