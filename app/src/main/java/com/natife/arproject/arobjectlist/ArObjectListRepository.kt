package com.natife.arproject.arobjectlist

import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model

class ArObjectListRepository : ArObjectListContract.Repository {
    private lateinit var listGeneral: MutableList<Model>

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
//        listFolder = java.util.ArrayList()
//        listFolder.add(Model(null, Model.FOLDER_TYPE, "folder 1", null, null, null))

        val listImage: ArrayList<Model> = java.util.ArrayList()
        listImage.add(Model(null, Model.IMAGE_TYPE, "model", R.drawable.model, "model.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "tricycle", R.drawable.tricycle, "tricycle.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "uranus", R.drawable.uranus, "Uranus.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "model2", R.drawable.model2, "model2.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "model3", R.drawable.model3, "model3.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "model4", R.drawable.model4, "model4.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "chair", R.drawable.chair, "Chair2.sfb", null))
        listImage.add(Model(null, Model.IMAGE_TYPE, "andy", R.drawable.andy, "andy.sfb", null))
        return listImage
    }


    override fun createGeneralList(listImage: List<Model>, listFolder: List<Model>): MutableList<Model> {
        listGeneral = java.util.ArrayList()
        if (!listFolder.isEmpty()) {
            listGeneral.add(Model(null, Model.TEXT_TYPE, "Папки", null, null, 1))
            listGeneral.addAll(listFolder)
        }
        listGeneral.add(Model(null, Model.TEXT_TYPE, "Файлы", null, null, 1))
        listGeneral.addAll(listImage)
        return listGeneral
    }

    override fun getGeneralList(): MutableList<Model> {
        return listGeneral
    }

}