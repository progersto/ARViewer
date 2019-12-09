package com.natife.arproject.aractivity

interface OnView {

    interface OnActivityDo {
        fun changeHelpScreen(helpStep: Int): Int
        fun finishStepHelp()
        fun progressBar(visible: Int)
        fun notSetHelp()
    }

    interface OnFragmentDo {
        fun showPlane()
    }
}