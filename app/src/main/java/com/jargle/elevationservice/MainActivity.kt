package com.jargle.elevationservice

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import android.widget.Button
import com.jargle.elevationservice.elevationservice.ElevationService

class MainActivity : AppCompatActivity() , View.OnClickListener{

    private val startServiceButton by bind<Button>(R.id.btn_start_service)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startServiceButton.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val id = p0!!.id
        if (id == R.id.btn_start_service) {
            startService(Intent(this, ElevationService::class.java))
        }

    }

    // Used to lazily bind the button.
    fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
        @Suppress("UNCHECKED_CAST")
        return lazy(LazyThreadSafetyMode.NONE){findViewById<T>(res)}
    }
}
