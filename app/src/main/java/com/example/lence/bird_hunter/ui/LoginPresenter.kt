package com.example.lence.bird_hunter.ui


import android.content.Context
import android.content.SharedPreferences
import android.util.Log

import com.example.lence.bird_hunter.api.App

import java.io.IOException

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPresenter(internal var mLoginMVP: LoginMVP) {

    fun autor(username: String, password: String) {
        App.api?.autor(username, password)?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.e("response", response.message() + " " + response.code())
                if (response.body() != null) {
                    try {

                        val s = response.body()!!.string()
                        Log.e("response id", s)
                        if (s != "false") {
                            mLoginMVP.start(s)
                        } else
                            mLoginMVP.show("неверный логин или пароль")
                        // Log.e("autor", response.body().string());
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                } else {
                    mLoginMVP.show(response.message() + " " + response.code())

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                mLoginMVP.show(t.toString())
            }
        })
    }
}
