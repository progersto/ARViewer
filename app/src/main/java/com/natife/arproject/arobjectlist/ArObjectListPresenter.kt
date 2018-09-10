package com.natife.arproject.arobjectlist

import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model
import com.natife.arproject.data.entityRoom.ModelDao
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class ArObjectListPresenter(private val mView: ArObjectListContract.View, private val modelDao: ModelDao) : ArObjectListContract.Presenter {
    private val mRepository: ArObjectListContract.Repository = ArObjectListRepository.getInstance()


    override fun insertModel(firstInit: Boolean) {
        doAsync {
            if (firstInit) {
                val imageList = mRepository.initList()
                modelDao.insert(*imageList.toTypedArray())
            }else{
                //todo
                val newFolder = Model(null, Model.FOLDER_TYPE, "Новая папка", null, null, null)
                modelDao.insert(newFolder)
            }
            uiThread {
                getGeneralList()
            }
        }
    }


    override fun getTitleFromDialog(position: Int): Int {
        var message: Int = -1
        when (mRepository.getGeneralList()[position].type) {
            Model.IMAGE_TYPE -> message = R.string.rename_file
            Model.FOLDER_TYPE -> message = R.string.rename_folder
        }
        return message
    }


    override fun getGeneralList() {
        doAsync {
            val list = modelDao.getImageList()
            uiThread {
                getFolderList(list)
            }
        }
    }

    private fun getFolderList(imageList: List<Model>) {
        doAsync {
            val folderList = modelDao.getFolderList()
            uiThread {
                val list = mRepository.createGeneralList(imageList, folderList)
                mView.createAdapter(list)
            }
        }
    }

    override fun updateModel(model: Model) {
        doAsync {
            modelDao.updateModel(model)
            uiThread {
                getGeneralList()
            }
        }
    }

    override fun deleteModel(model: Model) {
        doAsync {
            modelDao.delete(model)
            uiThread {
                getGeneralList()
            }
        }
    }
}