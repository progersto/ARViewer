package com.natife.arproject.aractivity

import android.content.Context
import android.graphics.Bitmap
import com.natife.arproject.ObjectForList
import java.io.File
import java.util.ArrayList

interface ArActivityContract {

    interface View {

    }

    interface Presenter {
        fun getListNode(): ArrayList<ObjectForList>
        fun createFileForIntent(flag: Boolean, bitmap: Bitmap, context: Context): File
        fun getBitmapFromView(view: android.view.View): Bitmap
        fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap
    }

    interface Repository {

    }

}