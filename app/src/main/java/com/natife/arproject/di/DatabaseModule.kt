package com.natife.arproject.di

import android.arch.persistence.room.Room
import android.content.Context
import com.natife.arproject.data.MyDatabase
import com.natife.arproject.data.entityRoom.FileDataDao
import com.natife.arproject.data.entityRoom.FolderDataDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(private val context: Context) {

    @Singleton
    @Provides
    fun provideContext(): Context {
        return context
    }

    @Singleton
    @Provides
    fun provideMyDatabase(context: Context): MyDatabase {
        return Room.databaseBuilder(context, MyDatabase::class.java, "my-db").build()
    }

    @Singleton
    @Provides
    fun provideFileDataDao(myDatabase: MyDatabase): FileDataDao {
        return myDatabase.fileDataDao()
    }

    @Singleton
    @Provides
    fun provideFolderDao(myDatabase: MyDatabase): FolderDataDao {
        return myDatabase.folderDataDao()
    }
}//DatabaseModule