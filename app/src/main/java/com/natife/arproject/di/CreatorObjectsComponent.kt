package com.natife.arproject.di

import com.natife.arproject.aractivity.CreatorObjects
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [CreatorObjectsModule::class])
interface CreatorObjectsComponent {

    //    fun inject(arActivity: ArActivity)
     fun getCreatorObjectsModule(): CreatorObjects
}