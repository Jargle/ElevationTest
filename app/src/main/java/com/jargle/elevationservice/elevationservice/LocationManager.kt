package com.jargle.elevationservice.elevationservice

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class LocationManager() : LocationListener {

    private var myLocation: Location? = null

    override fun onLocationChanged(p0: Location?) {
        myLocation = p0
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }
}
