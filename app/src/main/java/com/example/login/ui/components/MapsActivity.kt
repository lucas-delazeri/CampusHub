package com.example.login.ui.components

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.login.BuildConfig
import com.example.login.R
import com.example.login.databinding.ActivityMapsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.util.Locale

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private var selectedPoint: GeoPoint? = null

    private val cartoVoyager: ITileSource = XYTileSource(
        "CartoVoyager", 0, 19, 256, ".png?key=${BuildConfig.CARTO_API_KEY}",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
            "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
        ),
        "© OpenStreetMap contributors © CARTO"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val config = Configuration.getInstance()
        config.load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        config.userAgentValue = "CampusHubApp/1.0 (Android; com.example.login)"

        val basePath = File(applicationContext.filesDir, "osmdroid_v2")
        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = File(basePath, "tiles")

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cardSearch.bringToFront()

        val isPickerMode = intent.getBooleanExtra("picker_mode", false)
        val eventTitle = intent.getStringExtra("event_title") ?: if (isPickerMode) "Toque para escolher o local" else "Local do Evento"
        val eventLocation = intent.getStringExtra("event_location") ?: "CampusHub"
        val eventLat = intent.getDoubleExtra("event_lat", -23.550520)
        val eventLng = intent.getDoubleExtra("event_lng", -46.633308)

        val map = binding.map
        map.setTileSource(cartoVoyager)
        map.setMultiTouchControls(true)

        val startPoint = GeoPoint(eventLat, eventLng)
        selectedPoint = startPoint
        val marker = Marker(map).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = eventTitle
            subDescription = eventLocation
        }
        map.overlays.add(marker)

        // Setup search functionality
        binding.btnSearchLocation.setOnClickListener {
            val query = binding.etSearchLocation.text.toString()
            searchLocation(query, marker, map)
        }

        binding.etSearchLocation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchLocation.text.toString()
                searchLocation(query, marker, map)
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearchLocation.windowToken, 0)
                true
            } else {
                false
            }
        }

        if (isPickerMode) {
            binding.btnConfirmLocation.visibility = View.VISIBLE

            val mapEventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    selectedPoint = p
                    marker.position = p
                    marker.title = "Local Selecionado"
                    marker.subDescription = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", p.latitude, p.longitude)
                    marker.showInfoWindow()
                    map.invalidate()
                    return true
                }

                override fun longPressHelper(p: GeoPoint): Boolean {
                    selectedPoint = p
                    marker.position = p
                    marker.title = "Local Selecionado"
                    marker.subDescription = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", p.latitude, p.longitude)
                    marker.showInfoWindow()
                    map.invalidate()
                    return true
                }
            }

            val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
            map.overlays.add(0, mapEventsOverlay)

            binding.btnConfirmLocation.setOnClickListener {
                val currentPoint = selectedPoint ?: startPoint
                val resultIntent = Intent().apply {
                    putExtra("selected_lat", currentPoint.latitude)
                    putExtra("selected_lng", currentPoint.longitude)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        map.post {
            map.controller.setZoom(17.0)
            map.controller.setCenter(startPoint)
            map.invalidate()
        }
    }

    private fun searchLocation(query: String, marker: Marker, map: org.osmdroid.views.MapView) {
        if (query.isBlank()) return
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val point = GeoPoint(address.latitude, address.longitude)
                selectedPoint = point
                marker.position = point
                marker.title = address.featureName ?: query
                marker.subDescription = address.getAddressLine(0) ?: ""
                marker.showInfoWindow()
                map.controller.animateTo(point)
                map.controller.setZoom(17.0)
                map.invalidate()
            } else {
                Toast.makeText(this, "Localização não encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Pesquisa de local indisponível no momento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}
