package adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.songhub.databinding.SongItemBinding
import interfaces.SongCallback
import model.Song
import android.media.MediaPlayer

class SongAdapter(
    var songs: List<Song> = listOf(Song.Builder().name("no").build())
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var songCallback: SongCallback? = null

    // MediaPlayer instance to play the song
    private var mediaPlayer: MediaPlayer? = null

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    // Return the size of the dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return songs.size
    }

    // Get song at specific position
    fun getItem(position: Int) = songs[position]

    // Bind data to each item view
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        with(holder.binding) {
            songName.text = song.name
            songDuration.text = song.duration
            songIsPublic.text = if (song.private) "Public" else "Private"

            // Handle click event on the entire item
            songCard.setOnClickListener {
                // Stop the current media player if any song is already playing
                stopCurrentSong()

                // Play the clicked song
                songCallback?.SongClicked(song, position)
                //playSong(song)
            }

            // Optionally, handle image loading here if needed
            // ImageLoader.getInstance().loadImage(song.albumCoverUrl, songImageView)
        }
    }

    // ViewHolder class for holding references to views inside each item
    inner class SongViewHolder(val binding: SongItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Optionally, handle any specific button clicks, etc.
            binding.songplayButton.setOnClickListener {
                songCallback?.SongClicked(getItem(adapterPosition), adapterPosition)
            }
        }
    }

    // Play the song using MediaPlayer
    private fun playSong(song: Song) {
        // Assuming the song object has a URL or file path to the song (you should modify this based on your data model)
        val songUrl = song.url // This should be the actual URL or file path of the song

        try {
            // Initialize MediaPlayer with the song URL
            mediaPlayer = MediaPlayer().apply {
                setDataSource(songUrl)  // Set the data source for the MediaPlayer
                prepareAsync()           // Asynchronous preparation to avoid blocking the UI thread
                setOnPreparedListener {
                    start() // Start the song when prepared
                }
                setOnCompletionListener {
                    // Optionally, handle completion (e.g., reset the player or move to the next song)
                    stopCurrentSong() // Stop the player when the song is finished
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Stop the current song and release the MediaPlayer
    private fun stopCurrentSong() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()  // Stop playing the current song
                reset() // Reset the MediaPlayer
            }
            release() // Release the MediaPlayer resources
        }
        mediaPlayer = null
    }
}