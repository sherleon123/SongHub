package com.example.songhub

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import utilities.Firestore
import utilities.Location
import utilities.getAudioDuration
import utilities.storeAudioMetadata
import utilities.uploadAudio
import java.io.File

class AddSongActivity: AppCompatActivity() {
    private lateinit var addsong_LBL_header: MaterialTextView
    private lateinit var addsong_LBL_name: MaterialTextView
    private lateinit var addsong_LBL_songname: TextInputEditText
    private lateinit var addsong_BTN_privacy: MaterialButton
    private lateinit var addsong_BTN_audio: MaterialButton
    private lateinit var addsong_LBL_current: MaterialTextView
    private lateinit var addsong_BTN_create: MaterialButton
    private var public=false
    private var audioUri: Uri? = null
    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                audioUri = uri
                // Handle the selected audio file (e.g., display file name)
                handleAudioFile(uri)
                checkCreateButtonState()
            } else {
                // No file selected
                Toast.makeText(this, "No audio file selected", Toast.LENGTH_SHORT).show()
                checkCreateButtonState()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addsong)
        Firestore.init(this)
        findViews()
        initViews()
    }
    fun findViews()
    {
        addsong_LBL_header = findViewById(R.id.addsong_LBL_header)
        addsong_LBL_name = findViewById(R.id.addsong_LBL_name)
        addsong_LBL_songname = findViewById(R.id.addsong_LBL_songname)
        addsong_BTN_privacy = findViewById(R.id.addsong_BTN_privacy)
        addsong_BTN_audio = findViewById(R.id.addsong_BTN_audio)
        addsong_LBL_current = findViewById(R.id.addsong_LBL_current)
        addsong_BTN_create = findViewById(R.id.addsong_BTN_create)
        addsong_BTN_create.isEnabled=false

    }
    fun initViews()
    {
        addsong_BTN_privacy.setOnClickListener { view: View -> changeprivacy()  }
        addsong_BTN_audio.setOnClickListener { view: View -> select()  }
        addsong_BTN_create.setOnClickListener { view: View -> create()  }
        addsong_LBL_songname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // You can leave this empty or add logic if needed
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                checkCreateButtonState()  // Run your function when text is changed
            }

            override fun afterTextChanged(editable: Editable?) {
                // You can leave this empty or add logic if needed
            }
        })
    }
    fun create() {
        val audioUri = audioUri

        if (addsong_LBL_songname.text.toString().trim().isEmpty() || audioUri == null) {
            Toast.makeText(this, "Please provide both song name and audio file.", Toast.LENGTH_SHORT).show()
            return
        }
        Location.init(this)
        var locationManager = Location.getInstance()
        val location: Pair<Double,Double> = locationManager.getCurrentLocationForMap()

        // Step 1: Upload the audio file to Firebase Storage
        uploadAudio(audioUri) { audioUrl ->
            if (audioUrl != null) {
                // Step 2: Get the audio file duration
                getAudioDuration(this, audioUri) { duration ->
                    if (duration != null) {
                        // Step 3: Store the audio metadata in Firestore
                        storeAudioMetadata(this,audioUri, audioUrl,duration,addsong_LBL_songname.text.toString().trim(),public,location.first,location.second)

                        Toast.makeText(this, "Song uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to retrieve audio duration.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to upload audio file.", Toast.LENGTH_SHORT).show()
            }
        }
        val intent= Intent(this,MainActivity::class.java)
        startActivity(intent)
    }


    private fun checkCreateButtonState() {
        // Check if both the audioUri is not null and song name is not empty
        val songName = addsong_LBL_songname.text.toString().trim()
        addsong_BTN_create.isEnabled = !songName.isEmpty() && audioUri != null
    }
    fun select()
    {
        audioPicker()
    }
    fun changeprivacy()
    {
        if (addsong_BTN_privacy.text.equals("Click to make public"))
        {
            public=true
            addsong_BTN_privacy.text="Click to make private"
        }
        else
        {
            public=false
            addsong_BTN_privacy.text="Click to make public"
        }
    }

    fun audioPicker()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                1001
            )
        } else {
            // Permission already granted, proceed with file picker
            openAudioPicker()
        }
    }
    private fun openAudioPicker() {
        pickAudioLauncher.launch("audio/*")
    }
    private fun handleAudioFile(uri: Uri) {
        // Get the file name (optional: you can also retrieve other metadata like the duration)
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            val fileName = it.getString(nameIndex)

            // Display the file name (or do something else with the file)
            Toast.makeText(this, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
            addsong_LBL_current.text="Selected file: $fileName"
        }
    }

    // Handle the permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAudioPicker()
                    // Permission granted, proceed with file picker
                } else {
                    // Permission denied, handle accordingly
                }
            }
        }
    }
    private fun uploadAudio(uri: Uri, onComplete: (String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onComplete(null)
            return
        }
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.reference

        // Create a reference for the audio file in Firebase Storage
        val audioRef = storageReference.child("users/${user.uid}/audio_files/${System.currentTimeMillis()}-${uri.lastPathSegment}")

        // Use openInputStream to get the file from the content URI and upload it
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let {
            audioRef.putStream(it)
                .addOnSuccessListener {
                    audioRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        onComplete(downloadUrl.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    onComplete(null)
                }
        } ?: run {
            onComplete(null)
        }
    }


}