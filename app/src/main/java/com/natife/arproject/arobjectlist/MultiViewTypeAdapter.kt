package com.natife.arproject.arobjectlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model

class MultiViewTypeAdapter(
        private val list: MutableList<Model>,
        private val imageListener: OnItemImageListener,
        private val setFolder: Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        lateinit var recHolder: RecyclerView.ViewHolder

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


    class TextTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var txtType: TextView = itemView.findViewById(R.id.nameDirectory)
    }


    class FolderTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var nameFolder: TextView = itemView.findViewById(R.id.nameFolder)
        internal var menuFolderItem: RelativeLayout = itemView.findViewById(R.id.menuFolderItem)
    }


    class ImageTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var itemArImage: ImageView = itemView.findViewById(R.id.itemArImage)
        internal var nameItemImage: TextView = itemView.findViewById(R.id.nameItemImage)
        internal var menuImageItem: RelativeLayout = itemView.findViewById(R.id.menuImageItem)
    }


    override fun getItemViewType(position: Int): Int {
        when (list[position].type) {
            0 -> return Model.TEXT_TYPE
            1 -> return Model.FOLDER_TYPE
            2 -> return Model.IMAGE_TYPE
            else -> return -1
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, listPosition: Int) {
        when (list[listPosition].type) {
            Model.TEXT_TYPE ->
                (holder as TextTypeViewHolder).txtType.text = list[listPosition].name

            Model.FOLDER_TYPE -> {
                (holder as FolderTypeViewHolder).nameFolder.text = list[listPosition].name
                holder.menuFolderItem.setOnClickListener {
                    imageListener.onItemMenuClick(holder.adapterPosition)
                }
            }

            Model.IMAGE_TYPE -> {
                var name = list[listPosition].name
//                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                (holder as ImageTypeViewHolder).nameItemImage.text = name

                holder.menuImageItem.setOnClickListener {
                    imageListener.onItemMenuClick(holder.adapterPosition)
                }
                list[listPosition].image?.let { holder.itemArImage.setImageResource(it) }

//                val listener = View.OnClickListener {
//                    imageListener.onItemObjClick(holder.adapterPosition)
//                }
                holder.itemArImage.setOnClickListener { imageListener.onItemObjClick(holder.adapterPosition) }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}



