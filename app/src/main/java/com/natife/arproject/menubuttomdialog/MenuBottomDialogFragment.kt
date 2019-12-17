package com.natife.arproject.menubuttomdialog

import android.content.Context
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.natife.arproject.arobjectlist.OnMenuItemClick
import com.natife.arproject.R

class MenuBottomDialogFragment : BottomSheetDialogFragment() {
    private lateinit var onMenuItemClick: OnMenuItemClick

    companion object {
        fun newInstance(id: Int, name: String): MenuBottomDialogFragment {
            return MenuBottomDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt("pos", id)
                    putString("name", name)
                }
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onMenuItemClick = context as OnMenuItemClick
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.shit_dialog_ar_image, container, false)
        val dialogHeading: TextView = view.findViewById(R.id.dialogHeading)
        val btnDialogRename: TextView = view.findViewById(R.id.btnDialogRename)
        val btnDialogMove: TextView = view.findViewById(R.id.btnDialogMove)
        val btnDialogDelete: TextView = view.findViewById(R.id.btnDialogDelete)

        var name: String? = ""
        var pos: Int? = -1

        name = arguments?.getString("name")
        pos = arguments?.getInt("pos")



        dialogHeading.text = name
        btnDialogRename.setOnClickListener { _ ->
            pos?.also {
                onMenuItemClick.rename(it)
                dismiss()
            }

        }
        btnDialogMove.setOnClickListener {_ ->
            pos?.also {
                onMenuItemClick.move(it)
                dismiss()
            }
        }
        btnDialogDelete.setOnClickListener {_ ->
            pos?.also {
                onMenuItemClick.delete(it)
                dismiss()
            }
        }

        return view
    }
}