package com.natife.arproject.arobjectlist

import com.natife.arproject.R

class ArObjectListRepository: ArObjectListContract.Repository {

    private lateinit var listFolder: MutableList<Model>
    private lateinit var listImage: MutableList<Model>
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

    override fun initList(){
        listFolder = java.util.ArrayList()
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 1", null, null,null))
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 2", null, null,1))
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 3", null, null,1))
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 4", null, null,null))

        listImage = java.util.ArrayList()
        listImage.add(Model(Model.IMAGE_TYPE, "model", R.drawable.model, "model.sfb",1))
        listImage.add(Model(Model.IMAGE_TYPE, "tricycle", R.drawable.tricycle, "tricycle.sfb",1))
        listImage.add(Model(Model.IMAGE_TYPE, "uranus", R.drawable.uranus, "Uranus.sfb",1))
        listImage.add(Model(Model.IMAGE_TYPE, "model2", R.drawable.model2, "model2.sfb",1))
        listImage.add(Model(Model.IMAGE_TYPE, "model3", R.drawable.model3, "model3.sfb",null))
        listImage.add(Model(Model.IMAGE_TYPE, "model4", R.drawable.model4, "model4.sfb",null))
        listImage.add(Model(Model.IMAGE_TYPE, "chair", R.drawable.chair, "Chair2.sfb",1))
        listImage.add(Model(Model.IMAGE_TYPE, "andy", R.drawable.andy, "andy.sfb",null))
    }


    override fun createGeneralList(): MutableList<Model> {
        listGeneral = java.util.ArrayList()
        if (!listFolder.isEmpty()) {
            listGeneral.add(Model(Model.TEXT_TYPE,"Папки",null,null,1))
            listGeneral.addAll(listFolder)
        }
        listGeneral.add(Model(Model.TEXT_TYPE,"Файлы",null,null,1))
        listGeneral.addAll(listImage)
        return listGeneral
    }

    override fun getFolderList(): MutableList<Model> {
        return listFolder
    }

    override fun getImageList(): MutableList<Model> {
        return listImage
    }

    override fun getGeneralList(): MutableList<Model> {
        return listGeneral
    }

}