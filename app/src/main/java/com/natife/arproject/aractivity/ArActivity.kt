package com.natife.arproject.aractivity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.support.v4.app.ActivityCompat

import android.support.v7.app.AppCompatActivity

import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import android.widget.Toast.makeText
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException

import com.natife.arproject.R

import com.natife.arproject.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.File

class ArActivity : AppCompatActivity(), OnView {

    private var mUserRequestedInstall = true
    private var dialog: AlertDialog? = null
    private var name: String = ""
    private var resImage: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        name = intent.getStringExtra("name")
        resImage = intent.getIntExtra("resImage", 0)

        // Enable AR related functionality on ARCore supported devices only.
        checkArCoreApkAvailability()
        initView()
    }//onCreate


    private fun initView() {
        skipHelpIcon.setOnClickListener {
            finishStepHelp()
        }
        back.setOnClickListener {
            finish()
        }
        share.setOnClickListener {
            if (dialog == null) {
                createDialog()
            } else {
                dialog?.show()
            }
            getFile(false) { file ->
                startShare(file)
                dialog?.dismiss()
            }
        }
        screenShot.setOnClickListener {
            if (dialog == null) {
                createDialog()
            } else {
                dialog?.show()
            }
            askForPermission()
        }
    }//initView


    private fun createDialog() {
        dialog = AlertDialog.Builder(this)
                .setCancelable(false)
                .show()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(R.layout.dialog_create_screen)
    }//createDialog

    private fun getFile(flag: Boolean, callback: (file: File) -> Unit) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        doAsync {
            val helperActivity = HelperShare()
            val fragment: CustomArFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as CustomArFragment
            val firstBitmap = Bitmap.createBitmap(fragment.arSceneView.width, fragment.arSceneView.height, Bitmap.Config.ARGB_8888)
            val secondBitmap = helperActivity.getBitmapFromView(screen)
            PixelCopy.request(fragment.arSceneView, firstBitmap, {
                if (!this@ArActivity.isDestroyed) {

                    val finishBitmap = helperActivity.overlay(firstBitmap, secondBitmap)
                    doAsync {
                        val file = helperActivity.createFileForIntent(flag, finishBitmap, this@ArActivity)
                        uiThread {
                            callback(file)
                        }
                    }
                }
            }, Handler(handlerThread.looper))
        }
    }//getFile


    override fun onResume() {
        super.onResume()

        // Make sure ARCore is installed and up to date.
        try {
            when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    Log.d("", "Null in enum argument")
                }
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    // Ensures next invocation of requestInstall() will either return
                    // INSTALLED or throw an exception.
                    mUserRequestedInstall = false
                    return
                }
                null -> {
                    finish()
                    Log.d("", "Null in enum argument")
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            setResult(Activity.RESULT_OK, intent)
            finish()
            return
        } catch (e: Exception) {  // Current catch statements.
            Log.d("Exception", e.printStackTrace().toString())
            return
        }
    }

    private fun checkArCoreApkAvailability() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            Handler().postDelayed({ checkArCoreApkAvailability() }, 200)
        }
        if (!availability.isSupported) {
            makeText(this, getString(R.string.device_unsupported), Toast.LENGTH_LONG).show()
        }
    }


    override fun changeHelpScreen(helpStep: Int): Int {
        when (helpStep) {
            HELP_1 -> {
                skipHelpText.visibility = View.VISIBLE
                skipHelpIcon.visibility = View.VISIBLE
                textBody.visibility = View.VISIBLE
                textBody.setText(R.string.step1_help)
                imageNextDoIt.visibility = View.VISIBLE
                imageNextDoIt.setImageResource(R.drawable.ic_picture_3d)
            }
            HELP_2 -> {
                textHead.visibility = View.VISIBLE
                textHead.setText(R.string.step2_help)
                textBody.setText(R.string.step2_2_help)
                imageNextDoIt.setImageResource(R.drawable.ic_touch_1)
            }
            HELP_3 -> {
                textHead.setText(R.string.step3_help)
                textBody.setText(R.string.step3_2_help)
            }
            HELP_4 -> {
                textHead.setText(R.string.step4_help)
                textBody.setText(R.string.step4_2_help)
                imageNextDoIt.setImageResource(R.drawable.ic_touch_2)
            }
            HELP_5 -> {
                textHead.setText(R.string.step5_help)
                textBody.setText(R.string.step5_2_help)
                imageNextDoIt.setImageResource(R.drawable.ic_touch_3)
            }
            HELP_6 -> {
                finishStepHelp()
            }
        }
        return helpStep.inc()
    }

    override fun finishStepHelp() {
        skipHelpText.visibility = View.GONE
        skipHelpIcon.visibility = View.GONE
        textHead.visibility = View.GONE
        textBody.visibility = View.GONE
        imageNextDoIt.visibility = View.GONE
        back.visibility = View.VISIBLE
        logoTextImage.visibility = View.VISIBLE
        footer.visibility = View.VISIBLE
        fistInitAR(this, true)
        return
    }


    private fun startShare(file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_text))
            intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_URI))
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, resources.getString(R.string.share_via)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun askForPermission() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getFile(true) {
                        dialog?.dismiss()
                        makeText(this, resources.getString(R.string.screen_save), Toast.LENGTH_LONG).show()
                    }
                } else {
                    finish()
                }
                return
            }
        }
    }


    override fun progressBar(visible: Int) {
        progressBar.visibility = visible
    }

    override fun notSetHelp() {
        if (back.visibility == View.GONE) {
            back.visibility = View.VISIBLE
            logoTextImage.visibility = View.VISIBLE
            footer.visibility = View.VISIBLE
        }
    }
}
