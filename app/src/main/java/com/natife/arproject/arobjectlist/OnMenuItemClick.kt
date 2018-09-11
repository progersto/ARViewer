package com.natife.arproject.arobjectlist

interface OnMenuItemClick {
    fun rename(pos: Int)
    fun move(pos: Int)
    fun delete(pos: Int)
}