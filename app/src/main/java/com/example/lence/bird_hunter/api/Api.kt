package com.example.lence.bird_hunter.api


import com.example.lence.bird_hunter.model.Birds

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

interface Api {
    @GET("index.php?r=app/get-bird")
    fun get(): Call<Birds>

    @FormUrlEncoded
    @POST("index.php?r=app/auth")
    fun autor(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    //    @Multipart
    //    @POST("index.php?r=birds/coords-from-app")
    //    Call<ResponseBody> dispatch(@Query("id") int id,
    //                                @Query("x") String x,
    //                                @Query("y") String y,
    //                                @Query("bird") String bird,
    //                                @Part List<MultipartBody.Part> file);


    @Multipart
    @POST("index.php?r=app/coords-from-app")
    fun dispatch(
        @PartMap info: Map<String, @JvmSuppressWildcards RequestBody>,
        //Call<ResponseBody> dispatch(@PartMap() Map<String, String> info,
        @Part file: List<MultipartBody.Part>
    //@Part("image") file: List<RequestBody>
    ): Call<ResponseBody>

    //    @Multipart
    //    @POST("index.php?r=app/coords-from-app")
    //        //Call<ResponseBody> dispatch(@PartMap() Map<String, RequestBody> info,
    //    Call<ResponseBody> dispatch(@Query("user_id") String user_id,
    //                                @Query("x") String x,
    //                                @Query("y") String y,
    //                                @Query("bird_name") String bird_name,
    //                                @Part List<MultipartBody.Part> file);

}
