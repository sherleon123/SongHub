package com.example.songhub

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.songhub.ui.AllSongsFragment
import com.example.songhub.ui.MapFragment
import interfaces.SongMapCallback
import model.Song

class SongMapActivity: AppCompatActivity(){
    private lateinit var main_FRAME_list: FrameLayout

    private lateinit var main_FRAME_map: FrameLayout

    private lateinit var mapFragment: MapFragment

    private lateinit var allSongsFragment: AllSongsFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.song_map)

        findViews()
        initViews()
    }
    private fun findViews() {
        main_FRAME_list = findViewById(R.id.main_FRAME_list)
        main_FRAME_map = findViewById(R.id.main_FRAME_map)
    }

    private fun initViews() {
        mapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_FRAME_map, mapFragment)
            .commit()

        allSongsFragment = AllSongsFragment()


        allSongsFragment.songMapCallbackItemClicked = object : SongMapCallback{
            override fun SongMapItemClicked(lat: Double, lon: Double) {
                Log.d("clicksongmap","yes")
                mapFragment.zoom(lat,lon)
            }
        }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.main_FRAME_list ,allSongsFragment )
            .commit()

    }

}