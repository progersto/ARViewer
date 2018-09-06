package com.natife.arproject.arobjectlist

interface ArObjectListContract {
    interface View {


    }

    interface Presenter {
        fun firstInit(): MutableList<Model>
        fun getTitleFromDialog(position: Int): Int

    }

    interface Repository{
        fun initList()
        fun createGeneralList(): MutableList<Model>
        fun getFolderList(): MutableList<Model>
        fun getImageList(): MutableList<Model>
        fun getGeneralList(): MutableList<Model>
    }
}