package com.natife.arproject.arobjectlist

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.natife.arproject.MyApp
import com.natife.arproject.R
import com.natife.arproject.aractivity.ArActivity
import com.natife.arproject.data.entityRoom.Model
import com.natife.arproject.data.entityRoom.ModelDao
import com.natife.arproject.menubuttomdialog.MenuBottomDialogFragment
import com.natife.arproject.utils.REQUEST_AR_ACTIVITY
import com.natife.arproject.utils.fistInit
import com.natife.arproject.utils.isInit
import kotlinx.android.synthetic.main.activity_ar_object_list.*
import kotlinx.android.synthetic.main.item_ar.view.*
import org.jetbrains.anko.startActivityForResult
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
    private lateinit var imm: InputMethodManager
    private lateinit var newListModel: MutableList<Model>
//    private lateinit var session: Session
//
//    override fun onResume() {
//        super.onResume()
//        session = Session(this)
//        // IMPORTANT!!!  ArSceneView requires the `LATEST_CAMERA_IMAGE` non-blocking update mode.
//
//        val config = Config(session)
//        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
//        session.configure(config)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_object_list)

        MyApp.getDataBaseComponent().inject(this)
        mPresenter = ArObjectListPresenter(this, modelDao)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
            if (searchIcon.visibility == View.GONE) hideSearch()
            val dialog = Dialog(this)
            dialog.window!!.setLayout(1000, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.setContentView(R.layout.dialog_inform)
            dialog.show()
        }
        searchIcon.setOnClickListener {
            newListModel = java.util.ArrayList()
            searchIcon.visibility = View.GONE
            logoTextImage.visibility = View.GONE
            headText.visibility = View.GONE
            logoTextImage.visibility = View.GONE
            addFolder.visibility = View.GONE
            clearImage.visibility = View.VISIBLE
            searchText.visibility = View.VISIBLE
            searchText.addTextChangedListener(textWatcher)
            searchText.requestFocus()
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
        clearImage.setOnClickListener {
            hideSearch()
        }
        addFolder.setOnClickListener { _ ->
            addFlag = true
            rename(-1)
        }
        back.setOnClickListener { _ ->
            if (searchIcon.visibility == View.GONE) hideSearch()
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
                if (searchIcon.visibility == View.GONE) hideSearch()
                recyclerlist[position].id?.let { idMovable = it }

                val menuBottomDialogFragment =
                        MenuBottomDialogFragment.newInstance(idMovable, recyclerlist[position].name)
                menuBottomDialogFragment.show(supportFragmentManager, "menu_bottom_dialog_fragment")
            }

            override fun onItemObjClick(position: Int) {
                // 3D show
                if (searchIcon.visibility == View.GONE) hideSearch()
                startActivityForResult<ArActivity>(REQUEST_AR_ACTIVITY,
                        "link" to recyclerlist[position].vrLink,
                        "name" to recyclerlist[position].name)
            }

            override fun onItemObjLongClick(position: Int, res: Int) {
                // 2D show
                if (searchIcon.visibility == View.GONE) hideSearch()
                startActivityForResult<ArActivity>(REQUEST_AR_ACTIVITY,
                        "resImage" to res,
                        "name" to recyclerlist[position].name)
            }

            override fun onItemFolderClick(position: Int) {
                parentFolderId = recyclerlist[position].id
                folderNamesList.add(recyclerlist[position].name)

                if (searchIcon.visibility == View.GONE) hideSearch()
                mPresenter.getGeneralList(parentFolderId)

                mPresenter.getLifeDataModel().value?.let {
                    mPresenter.moveModel(it)
                }

                if (parentFolderId != null) {
                    lastIdList.add(parentFolderId!!)
                }
            }
        }
    }

    private fun hideSearch() {
        searchText.setText("")
        searchText.visibility = View.GONE
        clearImage.visibility = View.GONE
        logoTextImage.visibility = View.VISIBLE
        addFolder.visibility = View.VISIBLE
        searchIcon.visibility = View.VISIBLE
        imm.hideSoftInputFromWindow(searchText.windowToken, 0)
        newListModel.clear()
        mPresenter.getGeneralList(parentFolderId)
    }


    private val textWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            newListModel.clear()
            for (i in recyclerlist.indices) {
                var name = recyclerlist[i].name
                name = name.substring(0, 1).toLowerCase() + name.substring(1)
                if (name.contains(s, true) || recyclerlist[i].type == Model.TEXT_TYPE) {
                    newListModel.add(recyclerlist[i])
                }
            }
            createAdapter(newListModel)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {}
    }

    override fun createAdapter(newList: MutableList<Model>) {
        if (searchIcon.visibility == View.VISIBLE) {
            recyclerlist = java.util.ArrayList()
            recyclerlist.addAll(newList)
        }

        adapter = MultiViewTypeAdapter(newList, onItemImageListener, move, idMovable)
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
            if (searchIcon.visibility == View.VISIBLE) {
                headText.visibility = View.GONE
                logoTextImage.visibility = View.VISIBLE
            }
            info.visibility = View.VISIBLE
            back.visibility = View.GONE
        } else {
            info.visibility = View.GONE
            back.visibility = View.VISIBLE
            if (searchIcon.visibility == View.VISIBLE) {
                logoTextImage.visibility = View.GONE
                headText.visibility = View.VISIBLE
            }
            headText.text = folderNamesList[folderNamesList.size - 1]
        }
    }


    override fun rename(id: Int) {
        val title: Int
        val v: View = layoutInflater.inflate(R.layout.dialog_rename, null)
        val newName = v.findViewById<EditText>(R.id.newName)
        val name: String
        lateinit var model: Model
        if (id != -1) {
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


    override fun onBackPressed() {
        if (searchText.isFocused) {
            hideSearch()
        } else
            super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_AR_ACTIVITY && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, getString(R.string.for_continue_install_ARCore), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {

        val pos = recyclerAr.childCount -1
        for (i in 0..pos) {
            val holder = recyclerAr.getChildViewHolder(recyclerAr.getChildAt(i))
            if(holder.itemViewType == 2){
                holder.itemView.scene_container.removeAllViews()

            }
        }
        super.onDestroy()
    }
}

