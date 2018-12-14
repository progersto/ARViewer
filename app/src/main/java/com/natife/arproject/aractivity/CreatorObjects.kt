package com.natife.arproject.aractivity

import android.content.Context
import android.net.Uri
import android.view.View
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

class CreatorObjects(private val context: Context, private val onCreator: OnCreator) {

    fun createModelRenderable(name: String){
        ModelRenderable.builder()
                .setSource(context, Uri.parse(name))
                .build()
                .thenAccept { renderable -> onCreator.thenAcceptModel(renderable)  }
                .exceptionally {
                    onCreator.exceptionally()
                    null
                }
    }

    fun createViewRenderable(view2d: View){
        ViewRenderable.builder()
                .setView(context, view2d)
                .build()
                .thenAccept { renderable -> onCreator.thenAcceptView(renderable)  }
                .exceptionally {
                    onCreator.exceptionally()
                    null
                }
    }
}