package com.natife.arproject.aractivity

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode

class CreatorObjects(private val context: Context, private val onCreator: OnCreator, private val fragment: CustomArFragment) {
    private var minScale: Float = 0.25f
    private var maxScale: Float = 2f

    fun createModelRenderable(name: String) {
        ModelRenderable.builder()
                .setSource(context, Uri.parse(name))
                .build()
                .thenAccept { objectRenderable ->
                    val container3D = TransformableNode(fragment.transformationSystem)
                    container3D.renderable = objectRenderable
                    container3D.scaleController.minScale = minScale
                    container3D.scaleController.maxScale = maxScale
                    container3D.select()
                    onCreator.thenAcceptModel(container3D)
                }
                .exceptionally {
                    onCreator.exceptionally()
                    null
                }
    }

    fun createViewRenderable(view2d: View, anchorNodeParent: AnchorNode) {
        ViewRenderable.builder()
                .setView(context, view2d)
                .build()
                .thenAccept { objRenderable ->
                    val containerChild = TransformableNode(fragment.transformationSystem)
                    containerChild.renderable = objRenderable
                    containerChild.rotationController?.rotationRateDegrees = 0f//rotation prohibition

                    //create empty obj for parent
                    val objParent = createContainerParent(fragment, anchorNodeParent)
                    containerChild.setParent(objParent)
                    containerChild.setOnTouchListener { _, _ -> objParent!!.select() }

                    onCreator.thenAcceptView(containerChild, anchorNodeParent)
                }
                .exceptionally {
                    onCreator.exceptionally()
                    null
                }
    }

    private fun createContainerParent(fragment: CustomArFragment, anchorNodeParent: AnchorNode): TransformableNode? {
        //create empty obj for parent
        val containerParent = TransformableNode(fragment.transformationSystem)
        containerParent.setParent(anchorNodeParent)
        containerParent.select()
        return containerParent
    }
}