package com.natife.arproject.data.entityRoom

import android.arch.persistence.room.*


@Dao
interface ModelDao {

//    @get:Query("SELECT * FROM FileData ORDER BY id")
//    val listAccounts: Flowable<List<FileData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: ArrayList<Model>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertModel(model: Model)
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertModel(vararg model: Model)

    @Query("select * from Model where type = 2")
    fun getImageList(): List<Model>

    @Query("select * from Model where type = 1")
    fun getFolderList(): List<Model>

    @Update
    fun updateModel(model: Model)

    @Delete
    fun delete(model: Model)
//
    // update bet
//    @Query("UPDATE Model SET name = :name WHERE id =:id")
//    fun updateNameFile(name: String, id: Int)



}//FileDataDao
