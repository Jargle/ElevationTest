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
// No need to register this in the manifest, we explicitly send this to the service.
private const val SHUTDOWN_ACTION = "com.jargle.elevationservice.shutdownservice"

class ElevationService : Service(), LocationListener {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    init {
        df.maximumFractionDigits = 2
    }

    private var myLocation: Location? = null
    private lateinit var locationManager: LocationManager

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        /* Instantiate the notification builder. We keep it around so that updating the text is
         * easier later
         */
        notificationBuilder =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    NotificationCompat.Builder(this)
                } else {
                    // Required for Android O and above, notification channels for apps
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

        /* Set up the location manager for updates.
         */
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME, 0f, this)
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (SHUTDOWN_ACTION == intent?.action ?: "") {
            /* If it's the shutdown action from the pending intent notification click, stop self.
             * stopForeground only stops it from being a foreground service, and doesn't destroy it
             */
            stopForeground(true)
            stopSelf()
        } else {

            // Pending intent that is sent to this service when the notification is clicked
            val notificationIntent = Intent(this, ElevationService::class.java)
            notificationIntent.action = SHUTDOWN_ACTION
            val pendingIntent = PendingIntent.getService(this, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            // Notification setup
            notificationBuilder.setContentTitle("Elevation - Click to stop service")
                    .setContentText("Elevation loading")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    // Pending Intent sent out when notification clicked
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)

            // Start the service in the foreground with the notification
            startForeground(100, notificationBuilder.build())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove updates from the location manager for good cleanup.
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(p0: Location?) {
        myLocation = p0
        /* Update the notification with the correct text format when we get an update from the
         * system.
         */
        val formattedElevation = df.format(myLocation?.altitude)
        val date = simpleDateFormat.format(Date(myLocation?.time ?: 0L))
        notificationBuilder.setContentText("$formattedElevation at $date")
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
