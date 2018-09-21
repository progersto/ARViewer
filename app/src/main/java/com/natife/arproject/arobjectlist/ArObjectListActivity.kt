package com.natife.arproject.arobjectlist

import android.app.Dialog
import android.arch.lifecycle.Observer
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
import android.view.ViewGroup
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
    private var idMovable: Int = -1

    private lateinit var recyclerlist: MutableList<Model>
    private lateinit var onItemImageListener: OnItemImageListener
    private lateinit var adapter: MultiViewTypeAdapter
    @Inject
    lateinit var modelDao: ModelDao
    private var move: Boolean = false
    private var parentFolderId: Int? = null
    private var lastIdList: ArrayList<Int> = ArrayList()
    private var folderNamesList: ArrayList<String> = ArrayList()
    private var addFlag: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_object_list)

        MyApp.getDataBaseComponent().inject(this)
        mPresenter = ArObjectListPresenter(this, modelDao)

        initView()

        if (!isInit(this)) {
            mPresenter.insertModel("", true, parentFolderId)
            fistInit(this, true)// write in Preference
        } else {
            mPresenter.getGeneralList(parentFolderId)
        }

        //гардиент в статусбаре
        val window = this.window
        val background = ContextCompat.getDrawable(this, R.drawable.background_status_bar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.setBackgroundDrawable(background)

        mPresenter.getLifeDataModel().observe(this, Observer<Model> { value ->
            if (parentFolderId == value?.parentFolderId) {
                textMove.setTextColor(ContextCompat.getColor(this, R.color.colorTextMove))
                textMove.setOnClickListener(null)
            } else {
                textMove.setTextColor(ContextCompat.getColor(this, R.color.colorTextMoveSelected))
                textMove.setOnClickListener {
                    //write in db new parentFolderId
                    move = false
                    movePanel.visibility = View.GONE
                    if (value != null) {
                        value.parentFolderId = parentFolderId
                        mPresenter.updateModel(value, parentFolderId)
                    }
                }
            }
        })
    }//onCreate


    private fun initView() {
        info.setOnClickListener {
            val dialog = Dialog(this)
            dialog.window!!.setLayout(1000, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.setContentView(R.layout.dialog_inform)
            dialog.show()
        }
        addFolder.setOnClickListener { _ ->
            addFlag = true
            rename(-1)
        }
        back.setOnClickListener { _ ->
            if (lastIdList.size > 1) {
                folderNamesList.removeAt(folderNamesList.size - 1)
                val id = lastIdList[lastIdList.size - 2]
                mPresenter.getGeneralList(id)
                lastIdList.removeAt(lastIdList.size - 1)
                parentFolderId = id
            } else {
                mPresenter.getGeneralList(null)
                parentFolderId = null
                lastIdList.clear()
            }
            mPresenter.getLifeDataModel().value?.let {
                mPresenter.moveModel(it)
            }
        }
        textCancel.setOnClickListener {
            movePanel.visibility = View.GONE
            move = false
            mPresenter.getGeneralList(parentFolderId)
        }


        onItemImageListener = object : OnItemImageListener {

            override fun onItemMenuClick(position: Int) {
                recyclerlist[position].id?.let { idMovable = it }

                val menuBottomDialogFragment = MenuBottomDialogFragment.newInstance(idMovable, recyclerlist[position].name)
                menuBottomDialogFragment.show(supportFragmentManager, "menu_bottom_dialog_fragment")
            }

            //for show AR
            override fun onItemObjClick(position: Int) {
                val intent = Intent(this@ArObjectListActivity, MainActivity::class.java)
                intent.putExtra("name", recyclerlist[position].vrImage)
                startActivity(intent)
            }

            //for move item
            override fun onItemFolderClick(position: Int) {
                parentFolderId = recyclerlist[position].id
                mPresenter.getGeneralList(parentFolderId)

                //todo доделать тулбар
                folderNamesList.add(recyclerlist[position].name)

                mPresenter.getLifeDataModel().value?.let {
                    mPresenter.moveModel(it)
                }

                if (parentFolderId != null) {
                    lastIdList.add(parentFolderId!!)
                }
            }
        }
    }


    override fun createAdapter(newList: MutableList<Model>) {
        recyclerlist = newList
        adapter = MultiViewTypeAdapter(recyclerlist, onItemImageListener, move, idMovable)
        recyclerAr.layoutManager = GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

                override fun getSpanSize(position: Int): Int {
                    return when (recyclerAr.adapter?.getItemViewType(position)) {
                        Model.IMAGE_TYPE -> 1
                        else -> 2
                    }
                }
            }
        }
        changeHead()
    }


    private fun changeHead() {
        recyclerAr.itemAnimator = DefaultItemAnimator()
        recyclerAr.adapter = adapter
        if (parentFolderId == null) {
            headText.visibility = View.GONE
            logoTextImage.visibility = View.VISIBLE
            info.visibility = View.VISIBLE
            back.visibility = View.GONE
        } else {
            info.visibility = View.GONE
            back.visibility = View.VISIBLE
            logoTextImage.visibility = View.GONE
            headText.visibility = View.VISIBLE
            headText.text = folderNamesList[folderNamesList.size - 1]

        }
    }


    override fun rename(id: Int) {
        val title: Int
        val v: View = layoutInflater.inflate(R.layout.dialog_rename, null)
        val newName = v.findViewById<EditText>(R.id.newName)
        val name: String
       lateinit var model: Model
        if (id!= -1){
            model = getModelFromId(id)
        }

        if (addFlag) {
            name = resources.getString(R.string.newFolder)
            title = R.string.createFolder
        } else {
            title = mPresenter.getTitleFromDialog(model)
            name = model.name
        }
        newName.setText(name)
        newName.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        val dialog = android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setView(v)
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    if (addFlag) {
                        mPresenter.insertModel(newName.text.toString(), false, parentFolderId)
                        addFlag = false
                    } else {
                        model.name = newName.text.toString()
                        mPresenter.updateModel(model, parentFolderId)
                    }
                    imm.hideSoftInputFromWindow(newName.windowToken, 0)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    addFlag = false
                    imm.hideSoftInputFromWindow(newName.windowToken, 0)
                    dialog.dismiss()
                }
                .show()
        dialog.setCanceledOnTouchOutside(false)
    }

    override fun move(id: Int) {
        val model = getModelFromId(id)
        mPresenter.moveModel(model)
        movePanel.visibility = View.VISIBLE
        move = true
        mPresenter.getGeneralList(parentFolderId)
    }

    override fun delete(id: Int) {
        mPresenter.deleteModel(id, parentFolderId)
    }

    private fun getModelFromId(id: Int): Model {
        var model: Model? = null
        for (i in recyclerlist.indices) {
            if (recyclerlist[i].id == id) {
                model = recyclerlist[i]
                break
            }
        }
        return model!!
    }
}

