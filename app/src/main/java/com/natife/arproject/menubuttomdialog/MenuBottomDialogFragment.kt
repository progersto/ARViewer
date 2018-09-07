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
        fun newInstance(): MenuBottomDialogFragment {
            return MenuBottomDialogFragment()
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
        if (arguments != null) {
            name = arguments?.getString("name")
        }

        dialogHeading.text = name
        btnDialogRename.setOnClickListener {
            onMenuItemClick.rename()
            dismiss()
        }
        btnDialogMove.setOnClickListener {
            onMenuItemClick.move()
            dismiss()
        }
        btnDialogDelete.setOnClickListener {
            onMenuItemClick.delete()
            dismiss()
        }

        return view
    }
}