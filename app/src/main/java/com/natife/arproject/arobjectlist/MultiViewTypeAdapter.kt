package com.natife.arproject.arobjectlist

import android.graphics.drawable.Drawable
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.vr.dynamite.client.f
import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model


class MultiViewTypeAdapter(
        private val list: MutableList<Model>,
        private val imageListener: OnItemImageListener,
        private val move: Boolean,
        private val idMovable: Int)
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

        private var txtType: TextView = itemView.findViewById(R.id.nameDirectory)

        override fun onBindViewHolder(model: Model) {
            txtType.text = model.name
        }
    }

    inner class FolderTypeViewHolder(itemView: View) : AbsViewHolder(itemView) {

        internal var nameFolder: TextView = itemView.findViewById(R.id.nameFolder)
        internal var menuFolderItem: RelativeLayout = itemView.findViewById(R.id.menuFolderItem)
        internal var folderCardView: CardView = itemView.findViewById(R.id.folderCardView)
        internal var backgroundMoveFolder: RelativeLayout = itemView.findViewById(R.id.backgroundMoveFolder)

        override fun onBindViewHolder(model: Model) {
            nameFolder.text = model.name
            if (move) {
                menuFolderItem.visibility = View.GONE
                if (model.id != idMovable) {
                    folderCardView.setOnClickListener {
                        imageListener.onItemFolderClick(adapterPosition)
                    }
                    backgroundMoveFolder.visibility = View.GONE
                } else {
                    backgroundMoveFolder.visibility = View.VISIBLE
                }
            } else {
                menuFolderItem.visibility = View.VISIBLE
                menuFolderItem.setOnClickListener {
                    imageListener.onItemMenuClick(adapterPosition)
                }
                folderCardView.setOnClickListener {
                    imageListener.onItemFolderClick(adapterPosition)
                }
            }
        }
    }


    inner class ImageTypeViewHolder(itemView: View) : AbsViewHolder(itemView) {

        internal var itemArImage: ImageView = itemView.findViewById(R.id.itemArImage)
        internal var nameItemImage: TextView = itemView.findViewById(R.id.nameItemImage)
        internal var menuImageItem: RelativeLayout = itemView.findViewById(R.id.menuImageItem)
        internal var backgroundMove: RelativeLayout = itemView.findViewById(R.id.backgroundMove)

        override fun onBindViewHolder(model: Model) {
            nameItemImage.text = model.name
//            Picasso.get().load(model.image ?: 0).fit().into(itemArImage)
            Glide.with(itemView).load(model.image ?: 0)
                    .apply(RequestOptions().fitCenter())
                    .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }
            }).into(itemArImage)

            if (move) {
                menuImageItem.visibility = View.GONE
                backgroundMove.visibility = View.VISIBLE
            } else {
                menuImageItem.setOnClickListener {
                    imageListener.onItemMenuClick(adapterPosition)
                }
                itemArImage.setOnClickListener {
                    imageListener.onItemObjClick(adapterPosition)
                }
                itemArImage.setOnLongClickListener {
                    imageListener.onItemObjLongClick(adapterPosition, model.image!!)
                    return@setOnLongClickListener true
                }


                menuImageItem.visibility = View.VISIBLE
                backgroundMove.visibility = View.GONE
            }
        }
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



