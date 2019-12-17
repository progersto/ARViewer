package com.natife.arproject.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.natife.arproject.data.entityRoom.Model
import com.natife.arproject.data.entityRoom.ModelDao


@Database(entities = [(Model::class)], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {

//    abstract fun fileDataDao(): FileDataDao
//    abstract fun folderDataDao(): FolderDataDao
    abstract fun modelDao(): ModelDao

}//class MyDatabase
