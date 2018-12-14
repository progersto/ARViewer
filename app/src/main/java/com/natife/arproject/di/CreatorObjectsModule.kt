package com.natife.arproject.di

import android.content.Context
import com.natife.arproject.aractivity.CreatorObjects
import com.natife.arproject.aractivity.CustomArFragment
import com.natife.arproject.aractivity.OnCreator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CreatorObjectsModule (private val context: Context, private val onCreator: OnCreator, private  val fragment: CustomArFragment) {

    @Singleton
    @Provides
    fun provideCreatorObjects(): CreatorObjects {
        return CreatorObjects(context, onCreator, fragment)
    }
}