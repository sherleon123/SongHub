package com.example.songhub

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import utilities.Constants
import utilities.Location

class EntryMainActivity: AppCompatActivity() {
    private lateinit var entry_LBL_header: MaterialTextView
    private lateinit var entry_LBL_login: MaterialTextView
    private lateinit var entry_LBL_register: MaterialTextView
    private lateinit var entry_LBL_guest: MaterialTextView
    private lateinit var entry_BTN_login: MaterialButton
    private lateinit var entry_BTN_register: MaterialButton
    private lateinit var entry_BTN_guest: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Location.init(this)
        checkLocationAndPermissions()
        setContentView(R.layout.entry_main)
        FirebaseAuth.getInstance().signOut()

            findViews()
            initViews()

       // }

    }
    private fun findViews()
    {
        entry_LBL_header=findViewById(R.id.entry_LBL_header)
        entry_LBL_login=findViewById(R.id.entry_LBL_login)
        entry_LBL_register=findViewById(R.id.entry_LBL_register)
        entry_LBL_guest=findViewById(R.id.entry_LBL_guest)
        entry_BTN_login = findViewById(R.id.entry_BTN_login)
        entry_BTN_register=findViewById(R.id.entry_BTN_register)
        entry_BTN_guest=findViewById(R.id.entry_BTN_guest)
    }
    private fun initViews()
    {
        entry_BTN_login.setOnClickListener { view: View -> startActivity(1)  }
        entry_BTN_register.setOnClickListener {  view: View -> startActivity(2)   }
        entry_BTN_guest.setOnClickListener { view: View -> startActivity(3)   }

    }
    private fun startActivity(number:Int)
    {
        when (number) {
            1 -> {
                val intent= Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
            2 -> {
                val intent= Intent(this,RegisterActivity::class.java)
                startActivity(intent)
            }
            3 -> {//guest dosent add or delete songs
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
            4 -> {
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
            else -> {
            }
        }

    }
    private fun checkLocationAndPermissions() {
        if (!Location.getInstance().isLocationPermissionGranted()) {
            // Request permission if not granted
            Location.getInstance().requestLocationPermission(this, Constants.LOCATION.LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Check if location is enabled
            Location.getInstance().requestEnableLocation(this)
        }
    }
}