package utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import android.app.Activity
import android.content.Intent

class Location private constructor(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    companion object {
        @Volatile
        private var instance: Location? = null

        fun init(context: Context): Location {
            return instance ?: synchronized(this) {
                instance ?: Location(context).also { instance = it }
            }
        }

        fun getInstance(): Location {
            return instance ?: throw IllegalStateException(
                "LocationManagerHelper must be initialized"
            )
        }
    }

    // Function to check if location permission is granted
    fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request location permission
    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        }
    }

    // Function to check if location is enabled
    fun isLocationEnabled(): Boolean {
        val locationMode: Int
        try {
            locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            return false
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }

    // Function to request enabling location
    fun requestEnableLocation(activity: Activity) {
        if (!isLocationEnabled()) {
            Toast.makeText(context, "Location is disabled. Redirecting to settings...", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivity(intent)
        }
    }

    // Your existing function to get the current location
    fun getCurrentLocationForMap(): Pair<Double, Double> {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Missing location permission")
        }

        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        val bestLocation = when {
            gpsLocation != null && networkLocation != null ->
                if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
            gpsLocation != null -> gpsLocation
            networkLocation != null -> networkLocation
            else -> null
        }

        bestLocation?.let {
            return Pair(it.latitude, it.longitude)
        }

        throw Exception("Could not get location. Please ensure GPS is enabled.")
    }
}