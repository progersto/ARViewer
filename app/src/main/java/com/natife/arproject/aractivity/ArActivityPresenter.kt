package com.natife.arproject.aractivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Environment
import android.view.View
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.natife.arproject.ObjectForList
import com.natife.arproject.R
import com.natife.arproject.arobjectlist.ArObjectListContract
import com.natife.arproject.arobjectlist.ArObjectListRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ArActivityPresenter(private val mView: ArActivityContract.View) : ArActivityContract.Presenter {

    private val mRepository: ArObjectListContract.Repository = ArObjectListRepository.getInstance()


    override fun getListNode(): MutableList<ObjectForList> {
        return mRepository.getListNodeFromRepo()
    }


    @SuppressLint("SimpleDateFormat")
    override fun createFileForIntent(flag: Boolean, bitmap: Bitmap, context: Context): File {
        lateinit var file: File
        if (flag) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    context.resources.getString(R.string.app_name))// путь к файлу сохраняемого скрина
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val date = SimpleDateFormat("yyMMdd_HHmmss").format(Date())
            file = File(dir, "screen_$date.png")
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

    override fun createAnchorParent(hitResult: HitResult?, resolvedAnchor: Anchor?, arSceneView: ArSceneView): AnchorNode {
        // Create the Anchor Parent
        val anchorNodeParent = if (hitResult != null) {
            val anchorParent = hitResult.createAnchor()
            AnchorNode(anchorParent)
        } else {
            AnchorNode(resolvedAnchor)
        }
        anchorNodeParent.setParent(arSceneView.scene)
        return anchorNodeParent
    }

    override fun createAnchorChild(hitResult: HitResult?, resolvedAnchor: Anchor?,
                                   arSceneView: ArSceneView, fragment: CustomArFragment) {
        val anchorNodeChild: AnchorNode
        if (hitResult != null) {
            val anchorChild = hitResult.createAnchor()
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(anchorChild)
            mView.setCloudAnchor(newAnchor)//set cloudAnchor for HOSTING
            mView.setAnchorState(AppAnchorState.HOSTING)
            anchorNodeChild = AnchorNode(anchorChild)
        } else {
            anchorNodeChild = AnchorNode(resolvedAnchor)
        }
        anchorNodeChild.setParent(arSceneView.scene)
    }


}