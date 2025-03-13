package adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.songhub.databinding.SongItemBinding
import interfaces.SongCallback
import model.Song
import android.media.MediaPlayer
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import interfaces.SongMapCallback
private var googleMap: GoogleMap? = null
class SongMapAdapter(

    var songs: List<Song> = listOf(Song.Builder().name("no").build())
) : RecyclerView.Adapter<SongMapAdapter.SongViewHolder>() {

    var songMapCallback: SongMapCallback? = null


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

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
                Log.d("clickadapter","yes")
                // Play the clicked song
                songMapCallback?.SongMapItemClicked(song.lat,song.lon)


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
                songMapCallback?.SongMapItemClicked(getItem(adapterPosition).lat, getItem(adapterPosition).lon)
            }
        }
    }


}