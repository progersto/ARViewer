package com.natife.arproject.arobjectlist

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_ar_object_list.*
import android.view.WindowManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.EditText
import com.natife.arproject.*
import com.natife.arproject.main.MainActivity
import com.natife.arproject.menubuttomdialog.MenuBottomDialogFragment
import android.view.inputmethod.InputMethodManager
import com.natife.arproject.data.entityRoom.Model
import com.natife.arproject.data.entityRoom.ModelDao
import javax.inject.Inject


class ArObjectListActivity : AppCompatActivity(), ArObjectListContract.View, OnMenuItemClick {

    private lateinit var mPresenter: ArObjectListContract.Presenter
    private var localPosition: Int = -1
    private lateinit var listGeneral: MutableList<Model>
    private lateinit var onItemImageListener: OnItemImageListener
    private lateinit var adapter: MultiViewTypeAdapter
    @Inject
    lateinit var modelDao: ModelDao
    private var setFolder: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_object_list)

        MyApp.getDataBaseComponent().inject(this)
        mPresenter = ArObjectListPresenter(this, modelDao)

        initView()

        if (!isInit(this)) {
            mPresenter.firstInit()
//            mPresenter.insertModel()
            fistInit(this, true)// write in Preference
        } else {
            mPresenter.getGeneralList()
        }

        //гардиент в статусбаре
        val window = this.window
        val background = ContextCompat.getDrawable(this, R.drawable.background_status_bar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.setBackgroundDrawable(background)
    }//onCreate


    private fun initView() {
        addFolder.setOnClickListener {
            val newFolder = Model(null, Model.FOLDER_TYPE, "Новая папка", null, null, null)
            mPresenter.insertModel(newFolder)
        }

        onItemImageListener = object : OnItemImageListener {
            override fun onItemMenuClick(position: Int) {
                localPosition = position
                val menuBottomDialogFragment = MenuBottomDialogFragment.newInstance()
                val args = Bundle()
                args.putString("name", listGeneral[position].name)
                menuBottomDialogFragment.arguments = args
                menuBottomDialogFragment.show(supportFragmentManager, "menu_bottom_dialog_fragment")
            }

            override fun onItemObjClick(position: Int) {
                val intent = Intent(this@ArObjectListActivity, MainActivity::class.java)
                intent.putExtra("name", listGeneral[position].vrImage)
                startActivity(intent)
            }
        }
    }


    override fun createAdapter(generalList: MutableList<Model>) {
        listGeneral = generalList
        adapter = MultiViewTypeAdapter(listGeneral, onItemImageListener, setFolder)
        recyclerAr.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

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
    }


    override fun rename() {
        val title = mPresenter.getTitleFromDialog(localPosition)

        val v: View = layoutInflater.inflate(R.layout.dialog_rename, null)
        val newName = v.findViewById<EditText>(R.id.newName)
        newName.setText(listGeneral[localPosition].name)
        newName.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        val dialg = android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setView(v)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    val updatedModel = listGeneral[localPosition]
                    updatedModel.name = newName.text.toString()
                    mPresenter.updateModel(updatedModel)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        dialg.setOnDismissListener {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    override fun move() {

    }

    override fun delete() {
        mPresenter.deleteModel(listGeneral[localPosition])
    }
}

