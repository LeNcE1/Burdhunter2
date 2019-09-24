package com.example.lence.bird_hunter.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class SendPostWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val postBirdDao = App.postBirdDao ?: return Result.failure()
        val posts = postBirdDao.getPosts()
        if (posts.isEmpty()) return Result.success()

        posts.forEach { post ->
            val map = HashMap<String, RequestBody>()
            map["user_id"] =
                RequestBody.create(
                    okhttp3.MultipartBody.FORM,
                    post.user_id
                )
            map["x"] = RequestBody.create(okhttp3.MultipartBody.FORM, post.x)
            map["y"] = RequestBody.create(okhttp3.MultipartBody.FORM, post.y)
            map["bird_name"] =
                RequestBody.create(okhttp3.MultipartBody.FORM, post.bird_name)
            map["description"] =
                RequestBody.create(okhttp3.MultipartBody.FORM, post.description)

            val files = post.files.split("|").filter { s -> !s.isEmpty() }

            val fileParts = mutableListOf<MultipartBody.Part>()
            files.forEachIndexed { index, s ->
                val file = File(s)
                val requestBody = RequestBody.create(MediaType.parse("image"), file)
                fileParts.add(
                    MultipartBody.Part.createFormData(
                        "image_$index",
                        file.name,
                        requestBody
                    )
                )
            }

//            val fileParts =
//                files.map {
//                    val file = File(it)
//                    val requestBody = RequestBody.create(MediaType.parse("image"), file)
//                    MultipartBody.Part.createFormData("file" + file.name, file.name, requestBody)
//                }

            App.api?.run {
                dispatch(map, fileParts).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.e(
                            "sendBirdsResponse",
                            response.code().toString() + " " + response.message()
                        )
                        if (response.code() == 200) {
                            Toast.makeText(
                                context,
                                post.bird_name + " данные успешно отправлены",
                                Toast.LENGTH_SHORT
                            ).show()
                            postBirdDao.delete(post)
                        } else
                            Toast.makeText(
                                context,
                                response.code().toString() + " " + response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        // Toast.makeText(context, "onFailure" + t.message, Toast.LENGTH_SHORT).show()
                        Log.e(
                            "onFailure",
                            "onFailure" + t.message
                        )
                    }
                })
            }
        }

        return Result.retry()
    }
}