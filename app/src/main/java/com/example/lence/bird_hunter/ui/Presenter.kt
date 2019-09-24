package com.example.lence.bird_hunter.ui


import android.util.Log

import com.example.lence.bird_hunter.R
import com.example.lence.bird_hunter.api.App
import com.example.lence.bird_hunter.model.Birds

import java.util.HashMap

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.PartMap

class Presenter(internal var mMVP: MVP) {

    fun loadBirds() {
        App.api?.get()?.enqueue(object : Callback<Birds> {
            override fun onResponse(call: Call<Birds>, response: Response<Birds>) {
                //Log.e("Response",response.body().getBirds().toString());
                mMVP.addBirds(response.body()!!.birds)
            }

            override fun onFailure(call: Call<Birds>, t: Throwable) {
                Log.e("Response", t.toString())
                mMVP.showError()
            }
        })

    }

    fun sendBirds(map: HashMap<String, RequestBody>, file: List<MultipartBody.Part>) {
        //public void sendBirds(HashMap<String, String> map, List<MultipartBody.Part> file) {
        App.api?.run {
            dispatch(map, file).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.e(
                        "sendBirdsResponse",
                        response.code().toString() + " " + response.message()
                    )
                    if (response.code() == 200) {
                        mMVP.show("Данные успешно отправлены")
                        mMVP.clear()
                    } else
                    //mMVP.show("Ошибка отравки данных");
                        mMVP.show(response.code().toString() + " " + response.message())
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    mMVP.show("onFailure")
                }
            })
        }
        //     public void sendBirds(String user_id,String x,String y,String bird_name, List<MultipartBody.Part> file) {
        //        App.getApi().dispatch(user_id,x,y,bird_name,file).enqueue(new Callback<ResponseBody>() {
        //            @Override
        //            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        //                Log.e("sendBirdsResponse",response.code()+" "+response.message());
        //                if (response.code()==200){
        //                    mMVP.show("Данные успешно отправлены");
        //                    mMVP.clear();
        //                }
        //                else
        //                    mMVP.show("Ошибка отравки данных");
        //            }
        //
        //            @Override
        //            public void onFailure(Call<ResponseBody> call, Throwable t) {
        //                mMVP.show("onFailure");
        //            }
        //        });
    }

}
