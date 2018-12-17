package com.natife.arproject.aractivity

import com.natife.arproject.ObjectForList


interface ArActivityContract {

    interface View {

    }

    interface Presenter {
        fun getListNode(): MutableList<ObjectForList>
    }

    interface Repository {

    }

}