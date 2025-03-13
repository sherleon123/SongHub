package com.example.songhub.ui

import adapters.SongAdapter
import adapters.SongMapAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.songhub.R
import com.example.songhub.SongMapActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textview.MaterialTextView
import interfaces.SongMapCallback
import model.Song
import utilities.loadData
import utilities.loadPublicData


class MapFragment : Fragment(), OnMapReadyCallback,SongMapCallback {

    private var googleMap: GoogleMap? = null
    private lateinit var songsList: List<Song>
    private lateinit var songAdapter: SongMapAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_map, container, false)
        findViews(v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun findViews(v: View) {
    }

    fun zoom(lat: Double, lon: Double) {
        val location = LatLng(lat, lon)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        loadPublicData { songs ->
            songsList = songs

            // Log to check if songs are found
            if (songsList.isEmpty()) {
                Log.d("AllSongsFragment", "No songs found")
            } else {
                Log.d("AllSongsFragment", "Found ${songsList.size} songs")
            }

            // Set up the adapter after the data is loaded
            songAdapter = SongMapAdapter(songsList).apply {
                songMapCallback = this@MapFragment
            }
            songsList.forEach { scoreData ->
                val position = LatLng(scoreData.lat, scoreData.lon)
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Name: ${scoreData.name}")
                )
            }

            songsList.firstOrNull()?.let { firstScore ->
                val position = LatLng(firstScore.lat, firstScore.lon)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
            }
        }


    }


    override fun SongMapItemClicked(lat: Double, lon: Double) {
        Log.d("clickmap","yes")
        zoom(lat, lon)
    }
}
