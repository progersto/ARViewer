package com.natife.arproject.arobjectlist

import android.arch.lifecycle.MutableLiveData
import com.natife.arproject.entity.ObjectForList
import com.natife.arproject.data.entityRoom.Model

interface ArObjectListContract {

    interface View {
        fun createAdapter(newList: MutableList<Model>)
    }

    interface Presenter {
        fun getGeneralList(parentFolderId: Int?)
        fun getTitleFromDialog(model: Model): Int
        fun updateModel(model: Model, parentFolderId: Int?)
        fun deleteModel(id: Int, parentFolderId: Int?)
        fun insertModel(name: String, firstInit: Boolean, parentFolderId: Int?)
        fun getLifeDataModel(): MutableLiveData<Model>
        fun moveModel(model: Model)
    }

    interface Repository {
        fun initList(): MutableList<Model>
        fun createGeneralList(listImage: List<Model>, listFolder: List<Model>): MutableList<Model>
        fun getGeneralList(): MutableList<Model>
        fun getLifeDataModel(): MutableLiveData<Model>
        fun moveModel(model: Model)
        fun getListNodeFromRepo(): MutableList<ObjectForList>
    }
}