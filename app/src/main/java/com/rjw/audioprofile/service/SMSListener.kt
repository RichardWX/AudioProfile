package com.rjw.audioprofile.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import com.rjw.audioprofile.R
import com.rjw.audioprofile.activity.MainActivity

class SMSListener : BroadcastReceiver() {
    private val LOCATION_REQUEST = "Please send your location"
    private val LOCATION_DELAY = 5000L

    /**
     * Process incoming intents.
     * @param context The application context.
     * @param intent  The intent to be handled.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null && intent != null) {
            when(intent.action) {
                Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> try {
                    // Check the incoming SMS for message and potentially send a response...
                    (intent.extras!!.get("pdus") as Array<*>?)?.let { pdus ->
                        if(pdus.isNotEmpty()) {
                            val messages = arrayOfNulls<SmsMessage>(pdus.size)
                            for(i in pdus.indices) {
                                messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                            }

                            val sender = messages[0]!!.displayOriginatingAddress
                            val messageBody = StringBuilder()
                            // If SMS has several parts, lets combine it...
                            for(i in messages.indices) {
                                messageBody.append(messages[i]!!.messageBody)
                            }
                            val message = messageBody.toString()
                            if(message == LOCATION_REQUEST) {
                                startLocation(context)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    try {
                                        val sms = context.getSystemService(SmsManager::class.java) as SmsManager
                                        val newMessage = if(currentLocation == null) {
                                            context.getString(R.string.unknown_location)
                                        } else {
                                            context.getString(R.string.current_location, currentLocation!!.latitude, currentLocation!!.longitude)
                                        }
                                        sms.sendTextMessage(sender, null, newMessage, null, null)
                                        stopLocation()
                                    } catch(e: Exception) {
                                        // Cannot send SMS...
                                    }
                                }, LOCATION_DELAY)
                            }
                        }
                    }
                } catch(e: Throwable) {
                    // Do nothing.
                }
            }
        }
    }

    private var locationManager: LocationManager? = null
    private var currentLocation: Location? = null
    private var locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Update the current location to reflect the best guess of where we are.
            currentLocation = location
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun startLocation(context: Context) {
        try {
            if(locationManager == null) {
                locationManager = MainActivity.instance?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            }
            Thread {
                try {
                    locationManager?.let { locationManager ->
                        if(MainActivity.instance?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                        ) {
                            // Ensure that the screen is not turned off whilst the search is ongoing.
                            try {
                                if(MainActivity.instance?.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    // Request updates from both GPS and the local network.
                                    locationManager.removeUpdates(locationListener)
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener, MainActivity.instance?.mainLooper)
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener, MainActivity.instance?.mainLooper)
                                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                }
                            } catch(e: Exception) {
                                // Do nothing.
                            }
                        }
                    }
                } catch(e: Exception) {
                    // Do nothing.
                }
            }.start()
        } catch(e: Exception) {
            // Do nothing.
        }
    }

    private fun stopLocation() {
        locationManager?.removeUpdates(locationListener)
    }

}