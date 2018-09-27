package com.natife.arproject.aractivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Environment
import android.view.View
import com.natife.arproject.R
import com.natife.arproject.utils.getNumberScreen
import com.natife.arproject.utils.setNumberScreen
import java.io.File
import java.io.FileOutputStream


class ArActivityPresenter(private val mView: ArActivityContract.View):ArActivityContract.Presenter {

    override fun createFileForIntent(flag: Boolean, bitmap: Bitmap, context: Context): File {
        lateinit var file: File
        if (flag) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    context.resources.getString(R.string.app_name) )// путь к файлу сохраняемого скрина
            if (!dir.exists()) {
                dir.mkdirs()
            }
            var nunber = getNumberScreen(context)
            file = File(dir, "screen_ar_3d_viewr$nunber.png")
            setNumberScreen(context, ++nunber)
        } else {
            file = File(context.externalCacheDir, "ar_3d_viewr.png")
        }

        val fOut = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut)
        fOut.flush()
        fOut.close()
        file.setReadable(true, false)
        return file
    }

    override fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.TRANSPARENT)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    override fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        return bmOverlay
    }
}