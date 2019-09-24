package com.example.lence.bird_hunter.api

import android.app.Application
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.lence.bird_hunter.dateBase.AppDatabase
import com.example.lence.bird_hunter.dateBase.BirdDao
import com.example.lence.bird_hunter.dateBase.PostBirdDao
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val WORK_TAG = "WORK_TAG"

class App : Application() {
    private var retrofit: Retrofit? = null
    private var db: AppDatabase? = null

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "bird_hunter_database"
        ).allowMainThreadQueries().build()
        birdDao = db!!.birdDao()
        postBirdDao = db!!.postBirdDao()

        retrofit = Retrofit.Builder()
            .baseUrl("http://bird.bsu.ru/basic/web/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit!!.create(Api::class.java)

        sendPost()
    }


    companion object {
        var api: Api? = null
        var birdDao: BirdDao? = null
        var postBirdDao: PostBirdDao? = null
    }

}

fun sendPost() {
    val wm = WorkManager.getInstance()
    wm.cancelAllWorkByTag(WORK_TAG)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    wm.enqueue(
        OneTimeWorkRequestBuilder<SendPostWorker>()
            .addTag(WORK_TAG)
            .setConstraints(constraints)
            .build()
    )
}