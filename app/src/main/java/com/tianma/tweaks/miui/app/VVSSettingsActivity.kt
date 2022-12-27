package com.tianma.tweaks.miui.app

import android.app.NotificationManager
import android.content.Intent
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonSyntaxException
import com.tianma.tweaks.miui.databinding.DialogVvsOptionsBinding
import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*

class VVSSettingsActivity : AppCompatActivity() {
    private var _binding: DialogVvsOptionsBinding? = null
    val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        //region UI
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onCreate(savedInstanceState)
        AppSettings.initialize(applicationContext)
        _binding = DialogVvsOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvCancel.setOnClickListener { finish() }
        load()

    }

    private fun load() {
        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            meal = repository.getMeal(AppSettings.getString(AppSettings.Setting.USERNAME), calendar.time)

            withContext(Dispatchers.Main) {
                binding.cbBreakfast.isChecked = meal.isBreakfast
                binding.cbLunch.isChecked = meal.isLunch
                binding.cbDinner.isChecked = meal.isDinner
                binding.tvTitle.text = "Essen auswählen (" + meal.formattedDate + ")"
                binding.tvSave.setOnClickListener { onEdit() }

                showScreen()
            }
        }
    }


    private fun showScreen() {
        binding.tvGetInfo.visibility = View.GONE
        avdLoading!!.stop()
        binding.ivGetInfo.visibility = View.GONE
        binding.llMain.visibility = View.VISIBLE
        binding.tvSave.visibility = View.VISIBLE
    }

    private fun onSave() {
        val mealWithoutID = MealWithoutID(AppSettings.getString(AppSettings.Setting.USERNAME), updateMeta(), Encoder.formatDateSQL(calendar.time))
        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repository.addMeal(mealWithoutID)
            withContext(Dispatchers.Main) {
                val notificationManager = applicationContext.getSystemService(
                    NOTIFICATION_SERVICE
                ) as NotificationManager
                notificationManager.cancel(Values.NOTIFICATION_ID)
                if (binding.cbUpdate.isChecked && binding.llUpdate.visibility == View.VISIBLE)
                    MainScope().launch {
                        startActivity(Intent(this@VVSSettingsActivity, MainActivity::class.java))
                    }
                finish()
            }
        }
    }

    private fun onEdit() {
        meal.meta = updateMeta()
        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            repository.updateMeal(meal)
            withContext(Dispatchers.Main) {
                val notificationManager = applicationContext.getSystemService(
                    NOTIFICATION_SERVICE
                ) as NotificationManager
                notificationManager.cancel(Values.NOTIFICATION_ID)
                if (binding.cbUpdate.isChecked && binding.llUpdate.visibility == View.VISIBLE)
                    MainScope().launch {
                        startActivity(Intent(this@VVSSettingsActivity, MainActivity::class.java))
                    }
                finish()
            }
        }
    }

    private fun updateMeta(): Byte {
        val type = BitSet(3)
        type.set(Values.MEAL_BIT_BREAKFAST, binding.cbBreakfast.isChecked)
        type.set(Values.MEAL_BIT_LUNCH, binding.cbLunch.isChecked)
        type.set(Values.MEAL_BIT_DINNER, binding.cbDinner.isChecked)

        val byte: Byte = if (type.isEmpty) 0 else type.toByteArray()[0]
        return byte
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        if (throwable is SocketTimeoutException || throwable is UnknownHostException) {
            binding.tvGetInfo.setText(R.string.http_connection_timeout)
            binding.ivGetInfo.setImageDrawable(
                AppCompatResources.getDrawable(
                    applicationContext,
                    R.mipmap.rat_cable
                )
            )
            binding.tvSave.error = getString(R.string.http_connection_timeout)
        } else if (throwable is JsonSyntaxException) {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.tvSave.setOnClickListener { onSave() }
                showScreen()
            }
            return@CoroutineExceptionHandler
        }
        throwable.let { Snackbar.make(binding.root, Retrofitter.decodeException(it), Snackbar.LENGTH_LONG).show() }
    }
}