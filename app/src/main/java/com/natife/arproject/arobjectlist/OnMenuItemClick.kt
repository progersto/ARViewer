package com.natife.arproject.arobjectlist

interface OnMenuItemClick {
    fun rename(id: Int)
    fun move(id: Int)
    fun delete(id: Int)
}