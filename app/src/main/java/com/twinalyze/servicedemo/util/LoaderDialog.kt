package com.twinalyze.servicedemo.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.twinalyze.servicedemo.R

class LoaderDialog(context: Context) {

    private val dialog: Dialog = Dialog(context)

    init {
        dialog.setContentView(R.layout.dialog_loader)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun show() {
        if (!dialog.isShowing) dialog.show()
    }

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }
}