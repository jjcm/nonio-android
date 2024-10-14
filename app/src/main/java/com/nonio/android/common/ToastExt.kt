package com.nonio.android.common

import android.content.Context
import android.widget.Toast
import com.nonio.android.app.App

fun String.showToast(
    context: Context = App.app,
    duration: Int = Toast.LENGTH_SHORT,
) {
    Toast.makeText(context, this, duration).show()
}
