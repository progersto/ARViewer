package com.natife.arproject

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.ArrayList

class FoldersAdapter (context: Context, private val imageListener: OnItemImageListener, private val objectFolder: ArrayList<String>) :
        RecyclerView.Adapter<FoldersAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int {
        return objectFolder.size
    }//getItemCount

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }//getItemId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_folder, parent, false)
        return ViewHolder(view)
    } // onCreateViewHolder

    inner class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
//        internal var nameFolder: TextView = view.findViewById(R.id.nameFolder)
//        internal var menuItemFolder: ImageView = view.findViewById(R.id.menuItemFolder)
    }//class ViewHolder


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.nameFolder.text = objectFolder[position]
//
//        val listener = View.OnClickListener {
//            imageListener.onItemObjClick(holder.adapterPosition)
//        }
//        holder.menuItemFolder.setOnClickListener(listener)
    }//onBindViewHolder
}//class AdapterProductList