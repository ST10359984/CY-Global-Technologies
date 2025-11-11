package com.example.cyglobaltech.helpers

import android.app.AlertDialog
import android.content.Context

object MessageBox {
    fun show(context: Context, title: String, message: String, cancelable: Boolean = true, onOk: (() -> Unit)? = null) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(cancelable)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onOk?.invoke()
            }
            .show()
    }
}
