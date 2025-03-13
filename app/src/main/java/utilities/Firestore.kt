package utilities

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import model.Song
import java.io.File

class Firestore private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var instance: Firestore? = null

        fun init(context: Context): Firestore {
            return instance ?: synchronized(this) {
                instance ?: Firestore(context).also { instance = it }
            }
        }

    }
}

fun uploadAudio(context: Context, uri: Uri, fileName: String, onComplete: (String?) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Log.e("Upload", "User is not logged in.")
        onComplete(null)
        return
    }
    val storage = FirebaseStorage.getInstance()
    val storageReference = storage.reference

    // Use the provided file name directly
    val audioRef = storageReference.child("users/${user.uid}/audio_files/$fileName")

    // Use content resolver to open input stream from Uri
    val inputStream = context.contentResolver.openInputStream(uri)
    inputStream?.let {
        audioRef.putStream(it)
            .addOnSuccessListener {
                audioRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // On success, get the URL of the uploaded audio
                    onComplete(downloadUrl.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Upload", "Error uploading file", exception)
                onComplete(null)
            }
    } ?: run {
        Log.e("Upload", "Failed to open input stream from Uri.")
        onComplete(null)
    }
}
fun getAudioDuration(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    try {
        val mediaPlayer = MediaPlayer()

        // Set the data source using the context and the Uri
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()

        // Get the duration of the audio in milliseconds
        val durationInSeconds = mediaPlayer.duration / 1000
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60

        // Format the time as HH:mm:ss
        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // Release the media player
        mediaPlayer.release()

        // Return the formatted duration
        onComplete(formattedTime)
    } catch (e: Exception) {
        Log.e("Duration", "Error getting audio duration", e)
        onComplete(null) // If there's an error, return null
    }
}
fun storeAudioMetadata(context: Context, uri: Uri, audioUrl: String, duration: String, fileName: String,public:Boolean,lat:Double,lon:Double) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Log.e("StoreMetadata", "User is not logged in.")
        return
    }

    val db = FirebaseFirestore.getInstance()

    // Check for null or empty values in the metadata before adding to Firestore
    if (audioUrl.isEmpty() || fileName.isEmpty() || duration.isEmpty()) {
        Log.e("StoreMetadata", "One or more metadata fields are empty or null.")
        return
    }

    // Logging the metadata for debugging
    Log.d("StoreMetadata", "Audio Metadata: fileName=$fileName, audioUrl=$audioUrl, duration=$duration")

    // Prepare the metadata to be stored in Firestore
    val audioMetadata = hashMapOf(
        "audioFile" to audioUrl,
        "name" to fileName,
        "duration" to duration,
        "public" to public,
        "lat" to lat,
        "lon" to lon,
        "uploadedAt" to FieldValue.serverTimestamp()  // Timestamp when the data is stored
    )

    // Store the audio metadata in Firestore under the user's UID
    db.collection("users")
        .document(user.uid)
        .collection("audio_files")
        .add(audioMetadata)
        .addOnSuccessListener {
            Log.d("StoreMetadata", "Audio metadata stored successfully.")
        }
        .addOnFailureListener { exception ->
            Log.e("StoreMetadata", "Error storing audio metadata", exception)
        }
    Log.d("publicity","$public")
    if (public==true)
    {
        db.collection("public")
            .add(audioMetadata)
            .addOnSuccessListener {
                Log.d("StoreMetadata", "Audio metadata stored successfully.")
            }
            .addOnFailureListener { exception ->
                Log.e("StoreMetadata", "Error storing audio metadata", exception)
            }
    }
}
fun handleAudioUpload(context: Context, uri: Uri, fileName: String,public: Boolean,lat:Double,lon:Double) {
    // Step 1: Upload the audio file to Firebase Storage
    uploadAudio(context, uri, fileName) { audioUrl ->
        if (audioUrl != null) {
            // Step 2: Get the audio file duration
            getAudioDuration(context, uri) { duration ->
                if (duration != null) {
                    // Step 3: Store the audio metadata in Firestore
                    storeAudioMetadata(context, uri, audioUrl, duration, fileName,public,lat,lon)
                } else {
                    Log.e("Upload", "Failed to retrieve audio duration.")
                }
            }
        } else {
            Log.e("Upload", "Failed to upload audio file.")
        }
    }
}
fun loadData(onDataLoaded: (List<Song>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val songList = mutableListOf<Song>()
    if (user != null) {
        db.collection("users")
            .document(user.uid)
            .collection("audio_files")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val songName = document.getString("name") ?: ""
                    val duration = document.getString("duration") ?: ""
                    val lat = document.getDouble("lat") ?: 0.0
                    val lon = document.getDouble("lon") ?: 0.0
                    val isPublic = document.getBoolean("public") ?: false
                    val audioUrl = document.getString("audioFile") ?: ""
                    songList.add(Song.Builder().name(songName).private(isPublic).duration(duration).url(audioUrl).lon(lon).lat(lat).build())
                }
                // Once the data is loaded, pass it to the callback
                onDataLoaded(songList)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error fetching songs", exception)
                onDataLoaded(songList)  // Ensure it returns an empty list in case of failure
            }
    } else {
        onDataLoaded(songList)  // Ensure it returns an empty list if user is not logged in
    }
}
fun loadPublicData(onDataLoaded: (List<Song>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val songList = mutableListOf<Song>()

    // Query the 'public' collection to get all public songs
    db.collection("public")
        .get()
        .addOnSuccessListener { songSnapshot ->
            Log.d("Firestore", "Total public songs found: ${songSnapshot.size()}")

            // Loop through all public songs
            for (songDocument in songSnapshot) {
                val songName = songDocument.getString("name") ?: ""
                val duration = songDocument.getString("duration") ?: ""
                val lat = songDocument.getDouble("lat") ?: 0.0
                val lon = songDocument.getDouble("lon") ?: 0.0
                val audioUrl = songDocument.getString("audioFile") ?: ""

                // Add the song to the list
                songList.add(Song.Builder()
                    .name(songName)
                    .private(true)  // All songs are public in this case
                    .duration(duration)
                    .url(audioUrl)
                    .lat(lat)
                    .lon(lon)
                    .build())
            }

            // Once all songs are loaded, invoke the callback with the list
            onDataLoaded(songList)
        }
        .addOnFailureListener { exception ->
            Log.e("FirestoreError", "Error fetching public songs", exception)
            onDataLoaded(songList)  // Ensure it returns an empty list in case of failure
        }
}