package com.example.websocketdemo

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*;

class SecondeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        silver_gain2.setOnClickListener {
            object : View.OnClickListener {
                override fun onClick(v: View?) {
                    Toast.makeText(this@SecondeActivity, "11", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}