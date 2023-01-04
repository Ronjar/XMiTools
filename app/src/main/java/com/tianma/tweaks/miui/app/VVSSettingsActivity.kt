package com.tianma.tweaks.miui.app

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.tianma.tweaks.miui.data.http.entity.Trip
import com.tianma.tweaks.miui.data.sp.PreferenceContainer
import com.tianma.tweaks.miui.databinding.DialogVvsOptionsBinding


class VVSSettingsActivity : AppCompatActivity() {
    private var _binding: DialogVvsOptionsBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        //region UI
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onCreate(savedInstanceState)
        _binding = DialogVvsOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvCancel.setOnClickListener { finish() }
        load()
        //loadTo()
    }

    private fun load() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Trip.STATION.values().map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spFrom.adapter = adapter
        binding.spFrom.setSelection(Trip.STATION.values().indexOfFirst { it.name == PreferenceContainer.vvsOrigin })

        binding.spTo.adapter = adapter
        binding.spTo.setSelection(Trip.STATION.values().indexOfFirst { it.name == PreferenceContainer.vvsDestination })
    }

    private fun loadTo(){
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Trip.STATION.values().map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTo.adapter = adapter
        binding.spTo.setSelection(Trip.STATION.values().indexOfFirst { it.name == PreferenceContainer.vvsDestination })
    }

    private fun onSave() {
        PreferenceContainer.vvsOrigin = binding.spFrom.selectedItem.toString()
        PreferenceContainer.vvsDestination = binding.spTo.selectedItem.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}