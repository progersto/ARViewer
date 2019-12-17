package com.natife.arproject.base

interface BasePresenter<V> {

    fun viewAttach(view: V)

    fun viewDetach()
}


abstract  class  BasePresenterImpl<View>: BasePresenter<View> {

    protected var baseView: View? = null

    override fun viewAttach(view: View) {
        this.baseView = view
    }

    override fun viewDetach() {
        this.baseView = null
    }
}