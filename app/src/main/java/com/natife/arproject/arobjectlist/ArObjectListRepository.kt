package com.natife.arproject.arobjectlist

import android.arch.lifecycle.MutableLiveData
import com.natife.arproject.entity.ObjectForList
import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model

class ArObjectListRepository : ArObjectListContract.Repository {

    private lateinit var listGeneral: MutableList<Model>
    private val modelLiveData = MutableLiveData<Model>()
    private var listNode = mutableListOf<ObjectForList>()

    companion object {

        @Volatile
        private var INSTANCE: ArObjectListRepository? = null

        fun getInstance(): ArObjectListContract.Repository {
            if (INSTANCE == null) {
                synchronized(ArObjectListRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = ArObjectListRepository()
                    }
                }
            }
            return INSTANCE as ArObjectListContract.Repository
        }
    }


    override fun initList(): ArrayList<Model> {
        val listImage: ArrayList<Model> = java.util.ArrayList()

        listImage.add(Model(null,
                Model.IMAGE_TYPE,
                "Duck",
                R.drawable.model,
                "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/1.0/Duck/glTF/Duck.gltf",
                null))

        listImage.add(Model(null, Model.IMAGE_TYPE, "паук", R.drawable.tricycle,
                "https://raw.githubusercontent.com/progersto/test/master/app/src/main/assets/archive%20(3)/model.gltf", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "CesiumMan", R.drawable.uranus,
                "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/CesiumMan/glTF/CesiumMan.gltf", null))
        return listImage
    }


    override fun createGeneralList(listImage: List<Model>, listFolder: List<Model>): MutableList<Model> {
        listGeneral = java.util.ArrayList()
        if (!listFolder.isEmpty()) {
            listGeneral.add(Model(null, Model.TEXT_TYPE, "Папки", null, "", 1))
            listGeneral.addAll(listFolder)
        }
        listGeneral.add(Model(null, Model.TEXT_TYPE, "Файлы", null, "", 1))
        listGeneral.addAll(listImage)
        return listGeneral
    }


    override fun getGeneralList(): MutableList<Model> {
        return listGeneral
    }

    override fun getLifeDataModel(): MutableLiveData<Model> {
        return modelLiveData
    }

    override fun moveModel(model: Model) {
        modelLiveData.value = model
    }

    override fun getListNodeFromRepo(): MutableList<ObjectForList> {
        return listNode
    }
}