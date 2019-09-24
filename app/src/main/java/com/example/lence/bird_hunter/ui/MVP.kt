package com.example.lence.bird_hunter.ui


import java.util.ArrayList

interface MVP {
    fun addBirds(birds: List<String>)
    fun showError()
    fun show(res: String)
    fun clear()
}
