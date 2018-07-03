package com.hiray.rolling

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rolling_text_view.setOnClickListener{
            rolling_text_view.roll()
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        rolling_text_view.roll()
    }
}
