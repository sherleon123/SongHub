package com.example.songhub.ui

import adapters.SongAdapter
import adapters.SongMapAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songhub.R
import interfaces.SongMapCallback
import model.Song
import utilities.loadData
import utilities.loadPublicData

class AllSongsFragment : Fragment(), SongMapCallback {
    private lateinit var allsongs_LIST_songs: RecyclerView
    private lateinit var songsList: List<Song>
    private lateinit var songAdapter: SongMapAdapter
    var songMapCallbackItemClicked: SongMapCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_all_songs, container, false)
        findViews(v)

        // Set up RecyclerView layout manager
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        allsongs_LIST_songs.layoutManager = linearLayoutManager

        // Load data asynchronously
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
                songMapCallbackItemClicked = this@AllSongsFragment
            }

            // Set the adapter to RecyclerView
            allsongs_LIST_songs.adapter = songAdapter

        }

        return v
    }
    private fun findViews(v: View) {
        allsongs_LIST_songs=v.findViewById(R.id.allsongs_LIST_songs)

    }

    override fun SongMapItemClicked(lat: Double, lon: Double) {
        Log.d("clicksongfragment","yes")
        songMapCallbackItemClicked?.SongMapItemClicked(lat,lon)
    }

}