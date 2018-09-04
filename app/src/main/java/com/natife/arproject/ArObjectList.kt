package com.natife.arproject

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ar_object_list.*
import java.util.*
import android.view.WindowManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager


class ArObjectList : AppCompatActivity() {
//    private lateinit var objectsAdapter: ObjectsAdapter
    private lateinit var listFolder: MutableList<Model>
    private lateinit var listImage: MutableList<Model>
    private lateinit var listGeneral: MutableList<Model>
    private lateinit var onItemImageListener: OnItemImageListener
    private val objectAr = java.util.ArrayList(Arrays.asList("tricycle.sfb", "Uranus.sfb", "andy.sfb",
            "Chair2.sfb", "model.sfb", "model2.sfb", "model3.sfb", "model4.sfb"))
    private val objectArImage = java.util.ArrayList(Arrays.asList(R.drawable.tricycle, R.drawable.uranus, R.drawable.andy,
           R.drawable.chair, R.drawable.model, R.drawable.model2, R.drawable.model3, R.drawable.model4))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_object_list)

        initView()

        listFolder = java.util.ArrayList()
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 1",0,""))
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 2",0,""))
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 3",0,""))
        listFolder.add(Model(Model.FOLDER_TYPE, "folder 4",0,""))

        listImage = java.util.ArrayList()
        listImage.add(Model(Model.IMAGE_TYPE, "model", R.drawable.model, "model.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "tricycle", R.drawable.tricycle, "tricycle.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "uranus", R.drawable.uranus, "Uranus.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "model2", R.drawable.model2, "model2.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "model3", R.drawable.model3, "model3.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "model4", R.drawable.model4, "model4.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "chair", R.drawable.chair, "Chair2.sfb"))
        listImage.add(Model(Model.IMAGE_TYPE, "andy", R.drawable.andy, "andy.sfb"))

        createGeneralList()

        val adapter = MultiViewTypeAdapter(listGeneral, onItemImageListener)
        recyclerAr.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {
                    when (recyclerAr.adapter?.getItemViewType(position)) {
                        Model.IMAGE_TYPE -> return 1
                        else -> return 2
                    }
                }
            }
        }
        recyclerAr.itemAnimator = DefaultItemAnimator()
        recyclerAr.adapter = adapter

        //гардиент в статусбаре
        val window = this.window
        val background = ContextCompat.getDrawable(this, R.drawable.background_status_bar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.setBackgroundDrawable(background)
    }//onCreate


    private fun createGeneralList(){
        listGeneral = java.util.ArrayList()
        if (!listFolder.isEmpty()){
            listGeneral.add(Model(Model.TEXT_TYPE, "Папки", 0,""))
            listGeneral.addAll(listFolder)
        }
        listGeneral.add(Model(Model.TEXT_TYPE, "Файлы", 0,""))
        listGeneral.addAll(listImage)
    }


    private fun initView() {
        onItemImageListener = object : OnItemImageListener {
            override fun onItemObjClick(position: Int) {
                val intent = Intent(this@ArObjectList, MainActivity::class.java)
                intent.putExtra("name" , listGeneral[position].vrImage)
                startActivity(intent)
            }
        }
    }
}
