package com.natife.arproject.aractivity

import com.natife.arproject.ObjectForList
import com.natife.arproject.arobjectlist.ArObjectListContract
import com.natife.arproject.arobjectlist.ArObjectListRepository

class ArActivityPresenter(private val mView: ArActivityContract.View) : ArActivityContract.Presenter {

    private val mRepository: ArObjectListContract.Repository = ArObjectListRepository.getInstance()


    override fun getListNode(): MutableList<ObjectForList> {
        return mRepository.getListNodeFromRepo()
    }
}