package com.example.songhub.ui.dashboard

import adapters.SongAdapter
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songhub.AddSongActivity
import com.example.songhub.EntryMainActivity
import com.example.songhub.LoginActivity
import com.example.songhub.R
import com.example.songhub.SongMapActivity
import com.example.songhub.databinding.FragmentDashboardBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import interfaces.SongCallback
import model.Song
import utilities.loadData
import kotlin.random.Random

class MySongsFragment : Fragment(), SongCallback {
    private lateinit var lobby_BTN_map: MaterialButton
    private lateinit var lobby_BTN_addsong: MaterialButton
    private lateinit var lobby_BTN_signin: MaterialButton
    private lateinit var lobby_BTN_shuffle: MaterialButton
    private lateinit var lobby_LIST_songs: RecyclerView
    private lateinit var lobby_BTN_loop: MaterialButton
    private lateinit var lobby_BTN_previous: MaterialButton
    private lateinit var lobby_BTN_pause: MaterialButton
    private lateinit var lobby_BTN_resume: MaterialButton
    private lateinit var lobby_BTN_next: MaterialButton
    private lateinit var lobby_LBL_currentsong:MaterialTextView
    private lateinit var songAdapter: SongAdapter
    private lateinit var songsList: List<Song>
    private var mediaPlayer: MediaPlayer? = null
    private var shuffle:Boolean=false
    private var loop:Boolean=false
    private var previousSongs = mutableListOf<String>()  // To track played songs
    private var currentSongIndex = -1  // To track current song index
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                val duration = it.duration
                val currentTime = formatTime(currentPosition)
                val totalTime = formatTime(duration)
                if (currentSongIndex==-1||mediaPlayer==null)
                {
                    lobby_LBL_currentsong.text = "not playing"
                }
                else
                {
                    lobby_LBL_currentsong.text = songsList[currentSongIndex].name +"  $currentTime / $totalTime"
                }

            }
            handler.postDelayed(this, 1000)  // Update every second
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mySongsViewModel = inflater.inflate(R.layout.fragment_song_lobby, container, false)
        lobby_LBL_currentsong = mySongsViewModel.findViewById(R.id.lobby_LBL_currentsong)
        lobby_BTN_map = mySongsViewModel.findViewById(R.id.lobby_BTN_map)
        lobby_BTN_addsong = mySongsViewModel.findViewById(R.id.lobby_BTN_addsong)
        lobby_BTN_loop = mySongsViewModel.findViewById(R.id.lobby_BTN_loop)
        lobby_BTN_signin = mySongsViewModel.findViewById(R.id.lobby_BTN_signin)
        lobby_BTN_shuffle = mySongsViewModel.findViewById(R.id.lobby_BTN_shuffle)
        lobby_LIST_songs = mySongsViewModel.findViewById(R.id.lobby_LIST_songs)
        lobby_BTN_previous = mySongsViewModel.findViewById(R.id.lobby_BTN_previous)
        lobby_BTN_pause = mySongsViewModel.findViewById(R.id.lobby_BTN_pause)
        lobby_BTN_resume = mySongsViewModel.findViewById(R.id.lobby_BTN_resume)
        lobby_BTN_next = mySongsViewModel.findViewById(R.id.lobby_BTN_next)
        lobby_BTN_signin.text="Sign Out"
        lobby_BTN_loop.setBackgroundColor(Color.RED)
        lobby_BTN_loop.text="Loop OFF"
        lobby_BTN_map.setOnClickListener {
            val intent = Intent(requireActivity(), SongMapActivity::class.java)  // Use requireActivity() for fragment
            startActivity(intent)
        }

        lobby_BTN_addsong.setOnClickListener {
            val intent = Intent(requireActivity(), AddSongActivity::class.java)  // Use requireActivity() for fragment
            startActivity(intent)
        }

        lobby_BTN_signin.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireActivity(), EntryMainActivity::class.java)  // Use requireActivity() for fragment
            startActivity(intent)
        }

        lobby_BTN_shuffle.setOnClickListener {
            previousSongs.clear()
            shuffle=true
            playRandomSong()
        }
        lobby_BTN_loop.setOnClickListener {
            previousSongs.clear()
            if (loop==true)
            {
                loop=false
                lobby_BTN_loop.setBackgroundColor(Color.RED)
                lobby_BTN_loop.text="Loop OFF"
                mediaPlayer?.isLooping = false
            }
            else{
                loop=true
                lobby_BTN_loop.text="Loop ON"
                lobby_BTN_loop.setBackgroundColor(Color.BLACK)
            }
        }

        lobby_BTN_previous.setOnClickListener {
            if (previousSongs.isNotEmpty()) {
                // Backtrack to the last song in the previous songs list
                val lastSong = previousSongs.removeAt(previousSongs.lastIndex)
                currentSongIndex=songsList.indexOfFirst { it.url == lastSong }
                playSong(lastSong)
            } else {
            }
        }
        lobby_BTN_pause.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        }

        lobby_BTN_resume.setOnClickListener {
            if (mediaPlayer!=null&&mediaPlayer?.isPlaying()==false)
            {
                mediaPlayer?.start()
            }
        }

        lobby_BTN_next.setOnClickListener {
            playNextSong()
        }

        // RecyclerView setup
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        lobby_LIST_songs.layoutManager = linearLayoutManager

        // Load data asynchronously and set up RecyclerView
        loadData { songs ->
            songsList = songs
            if (songsList.isEmpty()) {
                Log.d("MySongsFragment", "No songs found")
            } else {
                Log.d("MySongsFragment", "Found ${songsList.size} songs")
            }

            // Set up the adapter after the data is loaded
            songAdapter = SongAdapter(songsList).apply {
                songCallback = this@MySongsFragment
            }
            lobby_LIST_songs.adapter = songAdapter
        }

        return mySongsViewModel
    }
    override fun SongClicked(song: Song, position: Int) {
        previousSongs.clear()
        shuffle=false
        // Handle song click, play the song or do something else
        // For now, let's just log it
        if (mediaPlayer?.isPlaying == true||mediaPlayer!=null) {
            mediaPlayer?.stop()  // Stop the current song
            mediaPlayer?.release()  // Release the current MediaPlayer
            mediaPlayer = null  // Set the MediaPlayer to null, so we can create a new one
            handler.removeCallbacks(updateTimerRunnable)
        }
        println("Song clicked: ${song.name} at position $position")
        val songUrl = song.url // Replace with your actual URL
        Log.d("songs",songUrl)
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer?.setDataSource(songUrl)  // Set the song's URL as the data source
            mediaPlayer?.prepareAsync()  // Prepare the media player asynchronously
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
                handler.post(updateTimerRunnable)
                currentSongIndex=position
                // Song is prepared and ready to play

            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireActivity(), "Error loading the song", Toast.LENGTH_SHORT).show()
        }
        mediaPlayer?.setOnCompletionListener {
            // Once the song is completed, decide what to do next
            handler.removeCallbacks(updateTimerRunnable)
            playNextSong()
        }

                  // Start playing the song


    }
    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    private fun playRandomSong() {
        val randomIndex = Random.nextInt(songsList.size)
        val randomSongUrl = songsList[randomIndex].url

        // Store the current song in the previous songs list if we're playing a new song
        if (currentSongIndex != randomIndex) {
            if (currentSongIndex != -1) {  // Don't add to previousSongs if it's the first song
                previousSongs.add(songsList[currentSongIndex].url)
            }
        }

        currentSongIndex = randomIndex
        playSong(randomSongUrl)
    }
    private fun playSong(songUrl: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            } else {
                handler.removeCallbacks(updateTimerRunnable)
                mediaPlayer?.reset()  // Reset the player to reuse it for the new song
            }

            mediaPlayer?.setDataSource(songUrl)
            mediaPlayer?.prepareAsync()

            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
                handler.post(updateTimerRunnable)
            }

            mediaPlayer?.setOnCompletionListener {
                handler.removeCallbacks(updateTimerRunnable)
                // Once the song is completed, shuffle the next song
                playRandomSong()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun playNextSong() {
        handler.removeCallbacks(updateTimerRunnable)

        if (loop)
        {
            Log.d("enter loop","$currentSongIndex")
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            handler.post(updateTimerRunnable)
        }
        else if (shuffle) {
            // If shuffle is on, select a random song
            playRandomSong()
        } else {
            // If shuffle is off, play the next song in the list
            playNextInOrder()
        }
    }
    private fun playNextInOrder() {
        // Ensure we don't go out of bounds of the song list
        val nextIndex = (currentSongIndex + 1) % songsList.size
        val nextSongUrl = songsList[nextIndex].url

        // Store the current song in the previous songs list if we're playing a new song
        if (currentSongIndex != nextIndex) {
            if (currentSongIndex != -1) {
                previousSongs.add(songsList[currentSongIndex].url)
            }
        }

        currentSongIndex = nextIndex
        playSong(nextSongUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()  // Release the MediaPlayer when the activity is destroyed
        mediaPlayer = null
        handler.removeCallbacks(updateTimerRunnable)
    }
}
