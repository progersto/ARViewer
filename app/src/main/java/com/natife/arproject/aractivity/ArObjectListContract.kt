package com.natife.arproject.aractivity

import android.content.Context
import android.graphics.Bitmap
import java.io.File

interface ArActivityContract {

    interface View {

    }

    interface Presenter {
        fun createFileForIntent(flag: Boolean, bitmap: Bitmap, context: Context): File
        fun getBitmapFromView(view: android.view.View): Bitmap
        fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap
    }

}