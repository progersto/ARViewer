package com.natife.arproject.aractivity

interface OnView {

    fun changeHelpScreen(helpStep: Int): Int
    fun finishStepHelp()
    fun progressBar(visible: Int)
    fun notSetHelp()
}