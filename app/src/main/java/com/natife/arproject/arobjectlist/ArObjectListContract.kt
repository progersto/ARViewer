package com.natife.arproject.arobjectlist

import android.arch.lifecycle.MutableLiveData
import com.natife.arproject.data.entityRoom.Model

interface ArObjectListContract {

    interface View {
        fun createAdapter(generalList: MutableList<Model>)
    }

    interface Presenter {
        fun getGeneralList(parentFolderId: Int?)
        fun getTitleFromDialog(model: Model): Int
        fun updateModel(model: Model, parentFolderId: Int?)
        fun deleteModel(model: Model, parentFolderId: Int?)
        fun insertModel(name: String, firstInit: Boolean, parentFolderId: Int?)
        fun getLifeDataModel(): MutableLiveData<Model>
        fun moveModel(model: Model)
    }

    interface Repository {
        fun initList(): ArrayList<Model>
        fun createGeneralList(listImage: List<Model>, listFolder: List<Model>): MutableList<Model>
        fun getGeneralList(): MutableList<Model>
        fun getLifeDataModel(): MutableLiveData<Model>
        fun moveModel(model: Model)
    }
}