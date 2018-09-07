package com.natife.arproject.arobjectlist

import com.natife.arproject.data.entityRoom.Model

interface ArObjectListContract {
    interface View {
//        fun openInitList()
        fun createAdapter(generalList:  MutableList<Model>)

    }

    interface Presenter {
        fun firstInit()
        fun getGeneralList()
        fun getTitleFromDialog(position: Int): Int
        fun updateModel(model: Model)
        fun deleteModel(model: Model)
        fun insertModel(model: Model)
        //        fun insertModel(vararg model: Model)
    }

    interface Repository {
        fun initList(): ArrayList<Model>
        fun createGeneralList(listImage: List<Model>, listFolder: List<Model>): MutableList<Model>


        fun getGeneralList(): MutableList<Model>
    }
}