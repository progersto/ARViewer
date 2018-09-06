package com.natife.arproject.arobjectlist

import com.natife.arproject.R

class ArObjectListPresenter (private val mView: ArObjectListContract.View) : ArObjectListContract.Presenter {
    private val mRepository: ArObjectListContract.Repository = ArObjectListRepository.getInstance()

    override fun firstInit(): MutableList<Model> {
        mRepository.initList()
        return mRepository.createGeneralList()
    }

    override fun getTitleFromDialog(position: Int): Int {
        var message :Int = -1
        when (mRepository.getGeneralList()[position].type) {
            Model.IMAGE_TYPE -> message =  R.string.rename_file
            Model.FOLDER_TYPE -> message =  R.string.rename_folder
        }
        return message
    }
}