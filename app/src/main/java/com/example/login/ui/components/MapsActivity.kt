package com.example.login.ui.components

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.login.BuildConfig
import com.example.login.databinding.ActivityMapsBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.io.File

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    private val cartoVoyager: ITileSource = XYTileSource(
        "CartoVoyager", 0, 19, 256, ".png?key=${BuildConfig.CARTO_API_KEY}",
        arrayOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val config = Configuration.getInstance()
        config.load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        config.userAgentValue = "CampusHubApp/1.0 (Android; com.example.login)"

        // Pasta de cache
        val basePath = File(applicationContext.filesDir, "osmdroid_v2")
        config.osmdroidBasePath = basePath
        config.osmdroidTileCache = File(basePath, "tiles")

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventTitle = intent.getStringExtra("event_title") ?: "Local do Evento"
        val eventLocation = intent.getStringExtra("event_location") ?: "CampusHub"

        val map = binding.map
        map.setTileSource(cartoVoyager)
        map.setMultiTouchControls(true)

        val startPoint = GeoPoint(-23.550520, -46.633308)

        map.post {
            map.controller.setZoom(17.0)
            map.controller.setCenter(startPoint)
            map.invalidate()
        }

        val marker = Marker(map).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = eventTitle
            subDescription = eventLocation
        }
        map.overlays.add(marker)
        map.invalidate()
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
