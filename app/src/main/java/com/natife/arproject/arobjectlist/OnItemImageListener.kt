package com.natife.arproject.arobjectlist

interface OnItemImageListener {
    fun onItemObjClick(position: Int)

    fun onItemObjLongClick(position: Int, res: Int)

    fun onItemMenuClick(position: Int)

    fun onItemFolderClick(position: Int)
}