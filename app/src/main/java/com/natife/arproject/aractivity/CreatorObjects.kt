package com.natife.arproject.aractivity

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode

class CreatorObjects(private val context: Context, private val onCreator: OnCreator, private  val fragment: CustomArFragment) {
    private var minScale: Float = 0.25f
    private var maxScale: Float = 2f

    fun createModelRenderable(name: String){
        ModelRenderable.builder()
                .setSource(context, Uri.parse(name))
                .build()
                .thenAccept { modelRenderable ->
                    val obj3D = TransformableNode(fragment.transformationSystem)
                    obj3D.renderable = modelRenderable
                    obj3D.scaleController.minScale = minScale
                    obj3D.scaleController.maxScale = maxScale
                    obj3D.select()
                    onCreator.thenAcceptModel(obj3D)
                }
                .exceptionally {
                    onCreator.exceptionally()
                    null
                }
    }

    fun createViewRenderable(view2d: View, anchorNodeParent: AnchorNode){
        ViewRenderable.builder()
                .setView(context, view2d)
                .build()
                .thenAccept { renderable ->
                    val objChild = TransformableNode(fragment.transformationSystem)
                    objChild.renderable = renderable
                    objChild.rotationController?.rotationRateDegrees = 0f//rotation prohibition

                    //create empty obj for parent
                    val objParent = createObjParent(fragment, anchorNodeParent)
                    objChild.setParent(objParent)
                    objChild.setOnTouchListener { _, _ -> objParent!!.select() }

                    onCreator.thenAcceptView(objChild, anchorNodeParent)  }
                .exceptionally {
                    onCreator.exceptionally()
                    null
                }
    }

    private fun createObjParent(fragment: CustomArFragment, anchorNodeParent: AnchorNode): TransformableNode? {
        //create empty obj for parent
        val objParent = TransformableNode(fragment.transformationSystem)
        objParent.setParent(anchorNodeParent)
        objParent.select()
        return objParent
    }

}