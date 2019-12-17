package com.natife.arproject.di

import com.natife.arproject.aractivity.CreatorObjects
import com.natife.arproject.aractivity.CustomArFragment
import com.natife.arproject.aractivity.OnCreator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CreatorObjectsModule (private val onCreator: OnCreator, private  val fragment: CustomArFragment) {

    @Singleton
    @Provides
    fun provideCreatorObjects(): CreatorObjects {
        return CreatorObjects(onCreator, fragment)
    }
}