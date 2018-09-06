package com.natife.arproject.arobjectlist

import com.natife.arproject.data.entityRoom.Model

interface ArObjectListContract {
    interface View {
        fun added()

    }

    interface Presenter {
        fun firstInit(): MutableList<Model>
        fun getTitleFromDialog(position: Int): Int
        fun addFiles()
    }

    interface Repository {
        fun initList()
        fun createGeneralList(): MutableList<Model>
        fun getFolderList(): MutableList<Model>
        fun getImageList(): MutableList<Model>
        fun getGeneralList(): MutableList<Model>
    }
}