package com.jargle.elevationservice.elevationservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.util.concurrent.TimeUnit

class ElevationService : Service(), LocationListener{

    private var myLocation : Location? = null
    private var locationManager: LocationManager? = null

    private val LOCATION_UPDATE_TIME = TimeUnit.SECONDS.toMillis(5)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val TAG: String = "ElevationService"

    override fun onCreate() {
        super.onCreate()
        if (locationManager == null) {
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        }

        try {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME, 0f, this)
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }
    }

    override fun onLocationChanged(p0: Location?) {
        myLocation = p0
        Toast.makeText(this, "New elevation: " + myLocation?.altitude, Toast.LENGTH_SHORT).show()
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }
}
