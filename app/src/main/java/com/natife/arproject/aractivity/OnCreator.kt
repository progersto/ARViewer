package com.natife.arproject.aractivity

import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

interface OnCreator {
    fun thenAcceptModel(modelRenderable: ModelRenderable)
    fun thenAcceptView(viewRenderable: ViewRenderable)
    fun exceptionally()
}