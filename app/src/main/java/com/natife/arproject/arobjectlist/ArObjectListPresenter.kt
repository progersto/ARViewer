package com.natife.arproject.arobjectlist

import com.natife.arproject.R
import com.natife.arproject.data.entityRoom.Model
import com.natife.arproject.data.entityRoom.ModelDao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ArObjectListPresenter (private val mView: ArObjectListContract.View, val modelDao: ModelDao) : ArObjectListContract.Presenter {
    private val mRepository: ArObjectListContract.Repository = ArObjectListRepository.getInstance()
    val compositeDisposable = CompositeDisposable()

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

    override fun addFiles() {
        val listFile = mRepository.getImageList() as ArrayList<Model>
        compositeDisposable.add(Observable.fromCallable { modelDao.insert(listFile[0]) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mView.added()
                })
    }
}