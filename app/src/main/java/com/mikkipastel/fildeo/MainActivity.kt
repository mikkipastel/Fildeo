package com.mikkipastel.fildeo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mikkipastel.fildeo.filter.EditVideoActivity
import com.mikkipastel.fildeo.utils.FileUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_PICK_VIDEO = 0x01
    private val UPLOAD_REQUEST_CODE = 0x02

    private val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
    private val REQUEST_STORAGE_CAMERA_PERMISSION = 102

    private var permissionsCamera: Array<String> = arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private var permissionsPickGallery: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCamera.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                openVideoCapture()
            } else {
                checkPermissionCamera()
            }
        }

        btnGallery.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                pickFromGallery()
            } else {
                checkPermissionPickGallery()
            }
        }
    }

    private fun openVideoCapture() {
        val videoCapture = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(videoCapture, REQUEST_PICK_VIDEO)
    }

    private fun checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsCamera, REQUEST_STORAGE_CAMERA_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsCamera, REQUEST_STORAGE_CAMERA_PERMISSION)
        }
    }

    private fun checkPermissionPickGallery() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsPickGallery, REQUEST_STORAGE_READ_ACCESS_PERMISSION)
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionsPickGallery, REQUEST_STORAGE_READ_ACCESS_PERMISSION)
        }
    }

    private fun pickFromGallery() {
        val intent = Intent()
        intent.apply {
            setTypeAndNormalize("video/*")
            action = Intent.ACTION_GET_CONTENT
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
                Intent.createChooser(intent,
                        getString(R.string.label_select_video)),
                REQUEST_PICK_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_VIDEO) {
                val selectedUri = data?.data
                if (selectedUri != null) {
                    startFilterActivity(selectedUri)
                } else {
                    Toast.makeText(this, R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startFilterActivity(uri: Uri) {
        val intent = EditVideoActivity.newIntent(this, FileUtils().getPath(this, uri))
        startActivityForResult(intent, UPLOAD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_READ_ACCESS_PERMISSION -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                            && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder(this)
                                .setMessage(getString(R.string.permission_title_rationale))
                                .setPositiveButton(getString(R.string.label_ok)) { _, _ ->
                                    onBackPressed()
                                    startInstalledAppDetailsActivity()
                                }
                                .setCancelable(false)
                                .create()
                                .show()
                    } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        pickFromGallery()
                    }
                }
            }
            REQUEST_STORAGE_CAMERA_PERMISSION -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                        && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder(this)
                            .setMessage(getString(R.string.permission_title_rationale))
                            .setPositiveButton(getString(R.string.label_ok)) { _, _ ->
                                onBackPressed()
                                startInstalledAppDetailsActivity()
                            }
                            .setCancelable(false)
                            .create()
                            .show()
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    openVideoCapture()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun startInstalledAppDetailsActivity() {
        val intent = Intent()
        intent.apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }
}
