package com.example.lab12_maps

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

val examplePolylineCoordinates = listOf(
    LatLng(-16.398866, -71.536961),
    LatLng(-16.4040102, -71.559611),
    LatLng(-16.412292, -71.530830)
)

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapType by remember { mutableStateOf(GoogleMap.MAP_TYPE_NORMAL) }
    var googleMapState by remember { mutableStateOf<GoogleMap?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // MapView para mostrar el mapa de Google
        AndroidView(
            factory = { mapView.apply { onCreate(null) } },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.onResume()
                mapView.getMapAsync { googleMap ->
                    googleMapState = googleMap
                    googleMap.mapType = mapType
                    googleMap.uiSettings.isZoomControlsEnabled = true

                    // Agregar un marcador en Plaza de Armas
                    val plazaArmas = LatLng(-16.39877, -71.53691)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(plazaArmas)
                            .title("Marcador en Plaza de Armas")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(plazaArmas))

                    // Al hacer clic en el mapa, agregar un marcador
                    googleMap.setOnMapClickListener { latLng ->
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Marcador"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                }
            }
        )

        // Caja que contiene el Dropdown en la parte superior izquierda
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            MapTypeDropdown(
                selectedMapType = mapType,
                onMapTypeSelected = { selectedMapType ->
                    mapType = selectedMapType
                    googleMapState?.mapType = selectedMapType
                }
            )
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
