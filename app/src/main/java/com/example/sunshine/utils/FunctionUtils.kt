package com.example.sunshine.utils

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.sunshine.R
import com.example.sunshine.databinding.LayoutProgressDialogBinding
import com.google.android.material.snackbar.Snackbar

@Suppress("unused")
object FunctionUtils {
    fun animateView(view: View, duration: Long = 500, repeat: Int = 0, techniques: Techniques = Techniques.Shake) {
        YoYo.with(techniques).duration(duration).repeat(repeat).playOn(view)
    }

    fun focusScreen(view: View) {
        view.setOnApplyWindowInsetsListener { _, windowInsets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                view.setPadding(0, 0, 0, imeHeight)
            }
            windowInsets
        }
    }

    fun toast(context: Context, msg: String, time: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, msg, time).show()
    }

    fun snackBar(view: View, msg: String, time: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(view, msg, time)
    }

    fun setUpDialog(message: String, context: Context): Dialog {
        val dialog = Dialog(context, R.style.CustomDialogTheme).apply {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setCancelable(false)
            val dialogBinding = LayoutProgressDialogBinding.inflate(layoutInflater)
            this.setContentView(dialogBinding.root)
            val width = (context.resources.displayMetrics.widthPixels * 0.80).toInt()
            this.window?.setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT)
            dialogBinding.dialogMessageTxt.text = message
        }
        return dialog
    }

    @Suppress("DEPRECATION")
    fun vibrateDevice(context: Context, time: Long = 300) {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                .vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(time)
        }
    }
}