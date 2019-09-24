package com.example.lence.bird_hunter.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.work.WorkManager
import com.example.lence.bird_hunter.R
import com.example.lence.bird_hunter.api.App
import com.example.lence.bird_hunter.api.WORK_TAG
import com.example.lence.bird_hunter.api.sendPost
import com.example.lence.bird_hunter.dateBase.Bird
import com.example.lence.bird_hunter.dateBase.PostBird
import com.example.lence.bird_hunter.utils.NetworkUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

private const val TYPE_PHOTO = 1
private const val UPDATE_INTERVAL: Long = 5000
private const val FASTEST_INTERVAL: Long = 5000

class MainActivity : AppCompatActivity(), MVP {

    private var locationRequest: LocationRequest? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var mPresenter: Presenter? = null

    private val birdDao = App.birdDao
    private val postBirdDao = App.postBirdDao
    private var dialog: ProgressDialog? = null
    private var files: MutableList<File> = mutableListOf()
    private var mSharedPreferences: SharedPreferences? = null
    private var file: File? = null

    private var gpsmyX = 0.0
    private var gpsmyY = 0.0

    private var mUri: Uri = generateFileUri(TYPE_PHOTO)
    private var mPhoto: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        mSharedPreferences = getSharedPreferences("bird", Context.MODE_PRIVATE)
        Log.e("share", mSharedPreferences!!.getString("id", "null"))
        dialog = ProgressDialog(this)
        dialog!!.setTitle("Обновление базы")
        dialog!!.isIndeterminate = true
        dialog!!.setCancelable(false)
        dialog!!.show()
        mPresenter = Presenter(this)
        if (NetworkUtil.isNetworkConnected(this)) {
            mPresenter?.loadBirds()
        } else {
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show()
        }
        files = ArrayList()

        locationRequest = LocationRequest()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = UPDATE_INTERVAL
        locationRequest!!.fastestInterval = FASTEST_INTERVAL

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    onShowLocationChange(location)
                }
            }
        }

        startLocationUpdates()

        viewFlipper.setOnClickListener {
            Log.e("click", "click")
            if (!del.isShown && files.size > 0) {
                del.show()
            } else {
                del.hide()
            }
        }
        arrowLeft.setOnClickListener {
            viewFlipper.inAnimation = AnimationUtils.loadAnimation(baseContext, R.anim.go_prev_in)
            viewFlipper.outAnimation = AnimationUtils.loadAnimation(baseContext, R.anim.go_prev_out)
            viewFlipper.showPrevious()
            if (viewFlipper.displayedChild == 0) {
                arrowLeft.visibility = View.GONE
            }
            del.hide()
            arrowRight.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp)
        }
        arrowRight.setOnClickListener {
            if (viewFlipper.displayedChild == viewFlipper.childCount - 1) {
                saveFullImage()
            } else {
                viewFlipper.inAnimation =
                    AnimationUtils.loadAnimation(baseContext, R.anim.go_next_in)
                viewFlipper.outAnimation =
                    AnimationUtils.loadAnimation(baseContext, R.anim.go_next_out)
                viewFlipper.showNext()
                del.hide()
                if (arrowLeft.visibility == View.GONE) {
                    arrowLeft.visibility = View.VISIBLE
                }
                if (viewFlipper.displayedChild == viewFlipper.childCount - 1) {
                    arrowRight.setImageResource(R.drawable.ic_add_black_24dp)
                }
            }

        }
        del.setOnClickListener {
            files.removeAt(viewFlipper.displayedChild)
            viewFlipper.removeViewAt(viewFlipper.displayedChild)
            del.hide()
            if (viewFlipper.childCount == 1) {
                arrowLeft.visibility = View.INVISIBLE
                arrowRight.visibility = View.VISIBLE
            }
            if (viewFlipper.childCount == 0) {
                nullImage.visibility = View.VISIBLE
                viewFlipper.visibility = View.INVISIBLE
                arrowLeft.visibility = View.INVISIBLE
                arrowRight.visibility = View.INVISIBLE
            }
        }
        nullImage.setOnClickListener {
            saveFullImage()
        }
        send.setOnClickListener {
            Log.e("send", files.size.toString())
            if (gpsmyX > 0 && gpsmyY > 0) {
                if (mSharedPreferences?.getString(
                        "id",
                        "null"
                    ) != "null" && autoText.text.isNotEmpty()
                ) {
                    send()

                } else
                    Toast.makeText(this, "Укажите вид птицы", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(this, "Мы не можем вас найти", Toast.LENGTH_SHORT).show()
        }

        redPoint.setOnClickListener {
            if (description.text.contains("stop")) {
                WorkManager.getInstance().cancelAllWorkByTag(WORK_TAG)
                Toast.makeText(this, "WorkManager stop", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun send() {
        Log.e(
            "CoordsImages", mSharedPreferences?.getString("id", "null")
                    + " " + gpsmyX.toString()
                    + " " + gpsmyY.toString()
                    + " " + autoText.text.toString()
        )

        val pathString = StringBuilder()
        files.forEach {
            pathString.append(it.path + "|")
        }

        val post = PostBird(
            0, mSharedPreferences?.getString("id", "null")!!,
            gpsmyX.toString(),
            gpsmyY.toString(),
            autoText.text.toString(),
            description.text.toString(),
            pathString.toString()
        )
        postBirdDao?.insert(post)
        Toast.makeText(this, "Отправка данных", Toast.LENGTH_SHORT).show()

        postBirdDao?.getPosts()?.get(0)?.files

        //todo
        clear()
        sendPost()

//        val map = HashMap<String, RequestBody>()
//        map["user_id"] =
//            RequestBody.create(
//                okhttp3.MultipartBody.FORM,
//                mSharedPreferences?.getString("id", "null")!!
//            )
//        map["x"] = RequestBody.create(okhttp3.MultipartBody.FORM, gpsmyX.toString())
//        map["y"] = RequestBody.create(okhttp3.MultipartBody.FORM, gpsmyY.toString())
//        map["bird_name"] =
//            RequestBody.create(okhttp3.MultipartBody.FORM, autoText.text.toString())
//        map["description"] =
//            RequestBody.create(okhttp3.MultipartBody.FORM, description.text.toString())
//
//        //RequestBody.create(MediaType.parse("image/*"),File(userAction.imagePath));
//        val fileParts =
//            files.map {
//                val requestBody = RequestBody.create(MediaType.parse("image"), it)
//                MultipartBody.Part.createFormData("image", it.name, requestBody)
//            }
//
//        dialog = ProgressDialog(this)
//        dialog!!.setTitle("Отправка данных")
//        dialog!!.isIndeterminate = true
//        dialog!!.setCancelable(false)
//        dialog!!.show()
//        mPresenter?.sendBirds(map, fileParts)
    }

    private fun onShowLocationChange(location: Location?) {
        this.gpsmyX = location?.latitude ?: 0.0
        this.gpsmyY = location?.longitude ?: 0.0
        Log.e("loc", "$gpsmyX $gpsmyY")
        locat.text = String.format("%.4f", gpsmyX) + " : " + String.format("%.4f", gpsmyY)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        if (((ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED))
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )

    }

    private fun saveFullImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            mUri = generateFileUri(TYPE_PHOTO)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            startActivityForResult(intent, TYPE_PHOTO)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


    private fun generateFileUri(type: Int): Uri {
        file = null
        createDirectory()
        when (type) {
            TYPE_PHOTO -> file =
                File(
                    Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path +
                            "/BirdHunter", "photo_" + System.currentTimeMillis() + ".jpg"
                )
        }
        Log.e("fileName", "fileName = " + file!!)
        return FileProvider.getUriForFile(
            this,
            "com.example.lence.bird_hunter.fileprovider",
            file!!
        )
    }

    private fun createDirectory(): File {
        val directory = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "BirdHunter"
        )
        if (!directory.exists())
            directory.mkdirs()
        return directory
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("camera Close", "dd")
        if (requestCode == TYPE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Log.e("data", "" + data)
                Log.e("file", "" + file!!.path)


                files.add(file!!)
                mPhoto = ImageView(this)
                mPhoto!!.setImageURI(mUri)
                nullImage.visibility = View.INVISIBLE
                viewFlipper.visibility = View.VISIBLE
                viewFlipper.addView(mPhoto)
                viewFlipper.showNext()
                if (viewFlipper.childCount > 0) {
                    arrowRight.visibility = View.VISIBLE
                }
                if (viewFlipper.childCount > 1) {
                    arrowLeft.visibility = View.VISIBLE
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("data", "Canceled")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun addBirds(birds: List<String>) {
        autoText.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line, birds
            )
        )

        birdDao?.insertAll(birds.map { Bird(it) })
        // Log.e("db", dbManager.getBirds().toString());
        dialog?.dismiss()
    }

    override fun showError() {
        if (!birdDao!!.getBirds().isEmpty()) {
            autoText.setAdapter(
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line, birdDao.getBirds()
                )
            )
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
        dialog?.dismiss()
    }

    @SuppressLint("ShowToast")
    override fun show(res: String) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
        dialog?.dismiss()
    }

    override fun clear() {
        autoText.setText("")
        description.setText("")
        files = ArrayList()
        viewFlipper.removeAllViews()
        nullImage.visibility = View.VISIBLE
        viewFlipper.visibility = View.INVISIBLE
        arrowLeft.visibility = View.INVISIBLE
        arrowRight.visibility = View.INVISIBLE
    }
}