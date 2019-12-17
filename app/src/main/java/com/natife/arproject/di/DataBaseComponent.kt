package com.natife.arproject.di

import com.natife.arproject.arobjectlist.ArObjectListActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class])
interface DataBaseComponent {

    fun inject(arObjectListActivity: ArObjectListActivity)
}

