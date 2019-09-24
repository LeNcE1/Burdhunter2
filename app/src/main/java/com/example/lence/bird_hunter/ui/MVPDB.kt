package com.example.lence.bird_hunter.ui


import com.example.lence.bird_hunter.model.Birds

import java.util.ArrayList

interface MVPDB {
    //    void insert(UserModel insertUser);
    val birds: List<String>

    //    void delete(String id);
    fun upDate(birds: List<String>)
}
