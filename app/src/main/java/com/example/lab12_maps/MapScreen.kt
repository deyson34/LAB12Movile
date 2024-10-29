package com.example.lab12_maps

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.core.app.ActivityCompat
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.graphics.Bitmap


val examplePolylineCoordinates = listOf(
    LatLng(-16.398866, -71.536961),
    LatLng(-16.4040102, -71.559611),
    LatLng(-16.412292, -71.530830)
)

fun resizeBitmap(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = image.width
    val height = image.height

    // Calcula la relación de aspecto
    val ratio = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    // Redimensiona la imagen
    return Bitmap.createScaledBitmap(image, newWidth, newHeight, false)
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapType by remember { mutableStateOf(GoogleMap.MAP_TYPE_NORMAL) }
    var googleMapState by remember { mutableStateOf<GoogleMap?>(null) }
    var userLocation by remember { mutableStateOf("Ubicación desconocida") }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView.apply { onCreate(null) } },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.onResume()
                mapView.getMapAsync { googleMap ->
                    googleMapState = googleMap
                    googleMap.mapType = mapType
                    googleMap.uiSettings.isZoomControlsEnabled = true

                    // Agregar un marcador en Plaza de Armas con imagen personalizada
                    val plazaArmas = LatLng(-16.39877, -71.53691)

                    // Redimensionar la imagen antes de crear el BitmapDescriptor
                    val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.humano)
                    val resizedBitmap = resizeBitmap(originalBitmap, 100, 100) // Ajusta el tamaño según necesites
                    val icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(plazaArmas)
                            .title("Marcador en Plaza de Armas")
                            .icon(icon) // Aquí aplicas el icono personalizado
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(plazaArmas))

                    // Verificar permisos de ubicación (código igual)
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.isMyLocationEnabled = true
                        googleMap.setOnMyLocationChangeListener { location ->
                            userLocation = "Ubicación: ${location.latitude}, ${location.longitude}"
                        }
                    } else {
                        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    }
                }
            }
        )

        // Dropdown y texto de ubicación se mantienen iguales
        Box(modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            MapTypeDropdown(selectedMapType = mapType, onMapTypeSelected = { selectedMapType ->
                mapType = selectedMapType
                googleMapState?.mapType = selectedMapType
            })
        }

        Box(modifier = Modifier.align(Alignment.TopStart).padding(top = 80.dp, start = 16.dp)) {
            Text(text = userLocation)
        }
    }
}

@Composable
fun MapTypeDropdown(selectedMapType: Int, onMapTypeSelected: (Int) -> Unit) {
    val tiposMapas = listOf(
        "Normal" to GoogleMap.MAP_TYPE_NORMAL,
        "Satélite" to GoogleMap.MAP_TYPE_SATELLITE,
        "Híbrido" to GoogleMap.MAP_TYPE_HYBRID,
        "Terreno" to GoogleMap.MAP_TYPE_TERRAIN,
        "Ninguno" to GoogleMap.MAP_TYPE_NONE
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedMapName by remember { mutableStateOf(tiposMapas.first { it.second == selectedMapType }.first) }

    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        // Botón para mostrar el Dropdown
        Button(onClick = { expanded = true }) {
            Text(selectedMapName)
        }

        // Menú desplegable
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tiposMapas.forEach { (name, type) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        selectedMapName = name
                        onMapTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
