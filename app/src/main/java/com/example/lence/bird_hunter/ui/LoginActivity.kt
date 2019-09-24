package com.example.lence.bird_hunter.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lence.bird_hunter.R
import com.example.lence.bird_hunter.utils.NetworkUtil
import kotlinx.android.synthetic.main.activity_login.*
import java.util.ArrayList

private const val ALL_PERMISSIONS_RESULT = 1011

class LoginActivity : AppCompatActivity(), LoginMVP {

    private var mDialog: ProgressDialog? = null
    private var mLoginPresenter: LoginPresenter = LoginPresenter(this)
    private var mSharedPreferences: SharedPreferences? = null

    private var permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()
    private val permissions = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.INTERNET)
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE)
        permissions.add(Manifest.permission.CAMERA)

        permissionsToRequest = permissionsToRequest(permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest!!.size > 0) {
                requestPermissions(permissionsToRequest!!.toTypedArray(), ALL_PERMISSIONS_RESULT)
            }
        }

        mSharedPreferences = getSharedPreferences("bird", Context.MODE_PRIVATE)
        //mLoginPresenter = LoginPresenter(this)
        mDialog = ProgressDialog(this)
        mDialog!!.setTitle("Выполняется вход")
        mDialog!!.setCancelable(true)

        button.setOnClickListener {
            if (login.text.isNotEmpty() && pass.text.isNotEmpty()) {
                if (NetworkUtil.isNetworkConnected(this)) {
                    mDialog?.show()
                    mLoginPresenter.autor(login.text.toString(), pass.text.toString())
                } else
                    Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(this, "Заполните поля", Toast.LENGTH_SHORT).show()


        }

    }

    @SuppressLint("ShowToast")
    override fun show(res: String) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
        mDialog?.dismiss()
    }

    override fun start(string: String) {
        mSharedPreferences?.edit()?.putString("id", string)?.commit()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun permissionsToRequest(wantedPermissions: ArrayList<String>): ArrayList<String> {
        val result = ArrayList<String>()

        for (perm in wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm)
            }
        }
        button.isEnabled = result.isEmpty()
        return result
    }

    private fun hasPermission(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else true

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                for (perm in permissionsToRequest!!) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm)
                    }
                }
                button.isEnabled = permissionsRejected.isEmpty()
            }
        }
    }
}
