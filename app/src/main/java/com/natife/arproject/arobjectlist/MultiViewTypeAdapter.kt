package com.natife.arproject.arobjectlist

import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model
import kotlinx.android.synthetic.main.item_ar.view.*
import kotlinx.android.synthetic.main.item_folder.view.*
import kotlinx.android.synthetic.main.item_name_directory.view.*
import org.jetbrains.anko.backgroundColor

class MultiViewTypeAdapter(
        private val list: MutableList<Model>,
        private val imageListener: OnItemImageListener,
        private val move: Boolean,
        private val idMovable: Int
//        private var session: Session
) : RecyclerView.Adapter<MultiViewTypeAdapter.AbsViewHolder>() {

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
        override fun onBindViewHolder(model: Model) {
            itemView.nameItemImage.text = model.name

            itemView.scene_container.removeAllViews()
            val sceneView = SceneView(itemView.context)

            if (move) {
                itemView.menuImageItem.visibility = View.GONE
                itemView.backgroundMove.visibility = View.VISIBLE
            } else {
                itemView.menuImageItem.setOnClickListener {
                    imageListener.onItemMenuClick(adapterPosition)
                }
                sceneView.setOnClickListener {
                    imageListener.onItemObjClick(adapterPosition)
                }
                sceneView.setOnLongClickListener {
                    imageListener.onItemObjLongClick(adapterPosition, model.image!!)
                    return@setOnLongClickListener true
                }

                itemView.menuImageItem.visibility = View.VISIBLE
                itemView.backgroundMove.visibility = View.GONE
            }

            sceneView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            sceneView.backgroundColor = ContextCompat.getColor(itemView.context, R.color.colorAccent)
            itemView.scene_container.addView(sceneView)

            val sceneFromContainer = itemView.scene_container.getChildAt(0) as SceneView
            renderObject(getRanderableSource(model.vrLink), model.name, sceneFromContainer) // Render the object

//            itemView.sceneView.setupSession(session)

        }

        private fun renderObject(source: RenderableSource?, name: String, sceneView: SceneView) {
            ModelRenderable.builder()
                    .setSource(itemView.context, source)
                    .build()
                    .thenAccept {
                        addNodeToScene(it, name, sceneView)
                    }
                    .exceptionally {
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

        private fun addNodeToScene(model: ModelRenderable?, modelName: String, sceneView: SceneView) {
            model?.let {
                Node().apply {
                    setParent(scene)
                    localPosition = Vector3(0f, 0f, -2.4f)
                    localScale = Vector3(3f, 3f, -2f)
                    name = modelName
                    renderable = it

                    sceneView.scene.addChild(this)
                    sceneView.resume()
//                    itemView.progressBar.visibility = View.GONE
                }
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



