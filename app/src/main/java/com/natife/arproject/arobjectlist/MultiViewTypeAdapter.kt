package com.natife.arproject.arobjectlist

import android.net.Uri
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.ar.core.Session
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model
import kotlinx.android.synthetic.main.item_ar.view.*
import kotlinx.android.synthetic.main.item_folder.view.*
import kotlinx.android.synthetic.main.item_name_directory.view.*

class MultiViewTypeAdapter(
        private val list: MutableList<Model>,
        private val imageListener: OnItemImageListener,
        private val move: Boolean,
        private val idMovable: Int
//        private var session: Session
)
    : RecyclerView.Adapter<MultiViewTypeAdapter.AbsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiViewTypeAdapter.AbsViewHolder {
        val view: View
        lateinit var recHolder: MultiViewTypeAdapter.AbsViewHolder

        when (viewType) {
            Model.TEXT_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_name_directory, parent, false)
                recHolder = TextTypeViewHolder(view)
            }
            Model.FOLDER_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_folder, parent, false)
                recHolder = FolderTypeViewHolder(view)
            }
            Model.IMAGE_TYPE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_ar, parent, false)
                recHolder = ImageTypeViewHolder(view)
            }
        }
        return recHolder
    }

    inner class TextTypeViewHolder(itemView: View) : AbsViewHolder(itemView) {

        override fun onBindViewHolder(model: Model) {
            itemView.nameDirectory.text = model.name
        }
    }

    inner class FolderTypeViewHolder(itemView: View) : AbsViewHolder(itemView) {

        override fun onBindViewHolder(model: Model) {
            itemView.nameFolder.text = model.name
            if (move) {
                itemView.menuFolderItem.visibility = View.GONE
                if (model.id != idMovable) {
                    itemView.folderCardView.setOnClickListener {
                        imageListener.onItemFolderClick(adapterPosition)
                    }
                    itemView.backgroundMoveFolder.visibility = View.GONE
                } else {
                    itemView.backgroundMoveFolder.visibility = View.VISIBLE
                }
            } else {
                itemView.menuFolderItem.visibility = View.VISIBLE
                itemView.menuFolderItem.setOnClickListener {
                    imageListener.onItemMenuClick(adapterPosition)
                }
                itemView.folderCardView.setOnClickListener {
                    imageListener.onItemFolderClick(adapterPosition)
                }
            }
        }
    }


    inner class ImageTypeViewHolder(itemView: View) : AbsViewHolder(itemView) {
        lateinit var scene: Scene
        lateinit var cupCakeNode: Node

        override fun onBindViewHolder(model: Model) {
            itemView.nameItemImage.text = model.name

            scene = itemView.sceneView.scene
//            itemView.sceneView.setupSession(session)
            itemView.sceneView.resume()
            renderObject( getRanderableSource(model.vrLink)) // Render the object

            if (move) {
                itemView.menuImageItem.visibility = View.GONE
                itemView.backgroundMove.visibility = View.VISIBLE
            } else {
                itemView.menuImageItem.setOnClickListener {
                    imageListener.onItemMenuClick(adapterPosition)
                }
                itemView.sceneView.setOnClickListener {
                    imageListener.onItemObjClick(adapterPosition)
                }
                itemView.sceneView.setOnLongClickListener {
                    imageListener.onItemObjLongClick(adapterPosition, model.image!!)
                    return@setOnLongClickListener true
                }

                itemView.menuImageItem.visibility = View.VISIBLE
                itemView.backgroundMove.visibility = View.GONE
            }
        }

        private fun renderObject(parse: RenderableSource?) {
            ModelRenderable.builder()
                    .setSource(itemView.context, parse)
                    .build()
                    .thenAccept {
                        addNodeToScene(it)
                    }
                    .exceptionally {
                        val builder = AlertDialog.Builder(itemView.context)
                        builder.setMessage(it.message)
                                .setTitle("error!")
                        val dialog = builder.create()
                        dialog.show()
                        return@exceptionally null
                    }

        }

        private fun getRanderableSource(link: String): RenderableSource? {
            return RenderableSource.builder()
                    .setSource(itemView.context,
                            Uri.parse(link),
                            RenderableSource.SourceType.GLTF2)
                    .setScale(0.5f)  // Scale the original model to 50%.
                    .setRecenterMode(RenderableSource.RecenterMode.CENTER)
                    .build()
        }

        private fun addNodeToScene(model: ModelRenderable?) {
            model?.let {
                cupCakeNode = Node().apply {
                    setParent(scene)
                    localPosition = Vector3(0f, 0f, -2.4f)
                    localScale = Vector3(3f, 3f, -2f)
                    renderable = it
                }

                scene.addChild(cupCakeNode)
            }
        }

//        private fun createNode(model: ModelRenderable?) {
//            val transformationSystem = TransformationSystem(itemView.sceneView.resources.displayMetrics, FootprintSelectionVisualizer())
//            val transformableNode = TransformableNode(transformationSystem).apply {
//                rotationController.isEnabled = true
//                scaleController.isEnabled = true
//                setParent(scene)
//                renderable = model
//            }
//            transformableNode.select()
//
//            scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
//                transformationSystem.onTouch(hitTestResult, motionEvent)
//            }
//            scene.addChild(transformableNode)
//        }
    }

    abstract inner class AbsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun onBindViewHolder(model: Model)
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].type) {
            0 -> Model.TEXT_TYPE
            1 -> Model.FOLDER_TYPE
            2 -> Model.IMAGE_TYPE
            else -> -1
        }
    }

    override fun onBindViewHolder(holder: MultiViewTypeAdapter.AbsViewHolder, listPosition: Int) {
        holder.onBindViewHolder(list[listPosition])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}



