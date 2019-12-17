package com.natife.arproject.aractivity

import android.net.Uri
import android.view.View
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode

class CreatorObjects(private val onCreator: OnCreator, private val fragment: CustomArFragment) {
    private var maxScale: Float = 2f
    private var minScale: Float = 0.25f

    fun createModelRenderable(link: String) {
        val cont = fragment.context!!
        ModelRenderable.builder()
                .setSource(cont, getRanderableSource(link))
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
                .setView(fragment.context, view2d)
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

    private fun getRanderableSource(link: String): RenderableSource? {
        return RenderableSource.builder().setSource(
                fragment.context,
                Uri.parse(link),
                RenderableSource.SourceType.GLTF2)
                .setScale(0.5f)  // Scale the original model to 50%.
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()
    }

    private fun createContainerParent(fragment: CustomArFragment, anchorNodeParent: AnchorNode): TransformableNode? {
        //create empty obj for parent
        val containerParent = TransformableNode(fragment.transformationSystem)
        containerParent.setParent(anchorNodeParent)
        containerParent.select()
        return containerParent
    }
}