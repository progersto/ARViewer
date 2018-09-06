package com.natife.arproject.data.entityRoom

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy

@Dao
interface ModelDao {

//    @get:Query("SELECT * FROM FileData ORDER BY id")
//    val listAccounts: Flowable<List<FileData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg model: Model)
//
//    @Update
//    fun updateFile(fileObj: FileData)
//
//    // update bet
//    @Query("UPDATE FileData SET name = :name WHERE id =:id")
//    fun updateNameFile(name: String, id: Int)



}//FileDataDao
