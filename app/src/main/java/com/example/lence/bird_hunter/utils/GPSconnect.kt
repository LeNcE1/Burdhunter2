package com.example.lence.bird_hunter.utils


import android.content.Context
import android.location.LocationManager
import android.widget.Toast

object GPSconnect {

    fun execute(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "Включите передачу геоданных", Toast.LENGTH_SHORT).show()
            return false
        } else
            return true

    }
}
