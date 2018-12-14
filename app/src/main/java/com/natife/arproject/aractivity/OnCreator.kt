package com.natife.arproject.aractivity

import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.TransformableNode

interface OnCreator {
    fun thenAcceptModel(transformableNode: TransformableNode)
    fun thenAcceptView(transformableNode: TransformableNode, anchorNodeParent: AnchorNode)
    fun exceptionally()
}