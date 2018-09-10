package com.natife.arproject.data.entityRoom

import android.arch.persistence.room.*


@Dao
interface ModelDao {

//    @get:Query("SELECT * FROM FileData ORDER BY id")
//    val listAccounts: Flowable<List<FileData>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg model: Model)

    @Query("select * from Model where type = 2 and parentFolderId is :parentFolderId")
    fun getImageList(parentFolderId: Int?): List<Model>

    @Query("select * from Model where type = 1 and parentFolderId is :parentFolderId")
    fun getFolderList(parentFolderId: Int?): List<Model>

    @Update
    fun updateModel(model: Model)

    @Delete
    fun delete(model: Model)
//
    // update bet
//    @Query("UPDATE Model SET name = :name WHERE id =:id")
//    fun updateNameFile(name: String, id: Int)



}//FileDataDao