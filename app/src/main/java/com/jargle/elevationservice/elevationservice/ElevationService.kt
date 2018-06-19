package com.jargle.elevationservice.elevationservice

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.jargle.elevationservice.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "ElevationService"
private val LOCATION_UPDATE_TIME = TimeUnit.SECONDS.toMillis(5)
private val df = DecimalFormat()
private val simpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
private const val SHUTDOWN_ACTION = "com.jargle.elevationservice.shutdownservice"

class ElevationService : Service(), LocationListener {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    init {
        df.maximumFractionDigits = 2
    }

    private var myLocation: Location? = null
    private lateinit var locationManager: LocationManager

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (SHUTDOWN_ACTION == intent?.action ?: "") {
            stopForeground(true)
            stopSelf()
        } else {

            val notificationIntent = Intent(this, ElevationService::class.java)
            notificationIntent.action = SHUTDOWN_ACTION
            val pendingIntent = PendingIntent.getService(this, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            notificationBuilder.setContentTitle("Elevation")
                    .setTicker("Elevation - Click to stop service")
                    .setContentText("Elevation loading")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)

            startForeground(100, notificationBuilder.build())
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        notificationBuilder =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    NotificationCompat.Builder(this)
                } else {
                    val channelId = "my_service"
                    val channelName = "My Background Service"
                    val chan = NotificationChannel(channelId, channelName,
                            NotificationManager.IMPORTANCE_NONE)
                    chan.lightColor = Color.BLUE
                    chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                    val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    service.createNotificationChannel(chan)
                    NotificationCompat.Builder(this, channelId)
                }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME, 0f, this)
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(p0: Location?) {
        myLocation = p0
        val formattedElevation = df.format(myLocation?.altitude)
        val date = simpleDateFormat.format(Date(myLocation?.time ?: 0L))
        notificationBuilder.setContentText("New elevation: $formattedElevation at $date")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(100, notificationBuilder.build())
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }
}
