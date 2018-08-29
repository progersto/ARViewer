package com.natife.arproject

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ar_object_list.*
import java.util.*
import android.view.WindowManager
import android.support.v4.content.ContextCompat


class ArObjectList : AppCompatActivity() {
    private lateinit var objectsAdapter: ObjectsAdapter
    private lateinit var onItemImageListener: OnItemImageListener
    private val objectAr = java.util.ArrayList(Arrays.asList("tricycle.sfb", "Uranus.sfb", "andy.sfb",
            "Chair2.sfb", "model.sfb", "model2.sfb", "model3.sfb", "model4.sfb"))
    private val objectArImage = java.util.ArrayList(Arrays.asList(R.drawable.tricycle, R.drawable.uranus, R.drawable.andy,
           R.drawable.chair, R.drawable.model, R.drawable.model2, R.drawable.model3, R.drawable.model4))


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_object_list)

        initView()

        objectsAdapter = ObjectsAdapter(this@ArObjectList, onItemImageListener, objectArImage)
        recyclerAr.adapter = objectsAdapter
        recyclerAr.isNestedScrollingEnabled = true


        val window = this.window
        val background = ContextCompat.getDrawable(this, R.drawable.background_status_bar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.setBackgroundDrawable(background)
    }

    private fun initView() {
        onItemImageListener = object : OnItemImageListener {
            override fun onItemObjClick(position: Int) {
                val intent = Intent(this@ArObjectList, MainActivity::class.java)
                intent.putExtra("name" , objectAr[position])
                startActivity(intent)
            }
        }
    }
}
