package com.androidutil.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidutil.app.databinding.ActivityMainBinding
import com.androidutil.util.DisplayUtils
import com.androidutil.util.LogUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val metrics = "DisplayMetrics: ${DisplayUtils.getDisplayWidth(this)} * ${DisplayUtils.getDisplayHeight(this)}\nRealMetrics: ${DisplayUtils.getRealWidth(this)} * ${DisplayUtils.getRealHeight(this)}"
        LogUtils.d(metrics)
        binding.tv.text = metrics

    }



}