package com.natife.arproject

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.*

class ObjectsAdapter(context: Context, private val imageListener: OnItemImageListener, private val objectArImage: ArrayList<Int>) : RecyclerView.Adapter<ObjectsAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return objectArImage.size
    }//getItemCount

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }//getItemId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_ar, parent, false)
        return ViewHolder(view)
    } // onCreateViewHolder

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
//        internal var image: ImageView = view.findViewById(R.id.itemArImage)
//        internal var nameItemImage: TextView = view.findViewById(R.id.nameItemImage)
//        internal var menuItem: RelativeLayout = view.findViewById(R.id.menuItemImage)
    }//class ViewHolder


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.image.setImageResource(objectArImage[position])
//        val listener = View.OnClickListener {
//            imageListener.onItemObjClick(holder.adapterPosition)
//        }
//        holder.image.setOnClickListener(listener)

//        val listener = View.OnClickListener {
//            imageListener.onItemObjClick(holder.adapterPosition)
//        }
//        holder.nameItemImage.setOnClickListener(listener)



    }//onBindViewHolder

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}//class AdapterProductList