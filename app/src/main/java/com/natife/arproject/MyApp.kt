package com.natife.arproject

import android.app.Application
import com.natife.arproject.di.DaggerDataBaseComponent
import com.natife.arproject.di.DataBaseComponent
import com.natife.arproject.di.DatabaseModule

class MyApp : Application() {
    private var dataBaseComponent: DataBaseComponent? = null
//    private var listsComponent: ListsComponent? = null

    override fun onCreate() {
        super.onCreate()
        dataBaseComponent = DaggerDataBaseComponent.builder()
                .databaseModule(DatabaseModule(applicationContext))
                .build()
//        listsComponent = DaggerListsComponent.builder()
//                .listsModule(ListsModule(applicationContext))
//                .build()
    }

    fun app(): MyApp {
        return this
    }

    fun getDataBaseComponent(): DataBaseComponent? {
        return dataBaseComponent
    }

//    fun getListComponent(): ListsComponent? {
//        return listsComponent
//    }
}//class MyApp

