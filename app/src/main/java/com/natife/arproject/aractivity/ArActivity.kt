package com.natife.arproject.aractivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene

import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.*
import com.natife.arproject.R
import com.natife.arproject.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class ArActivity : AppCompatActivity(), Scene.OnUpdateListener, ArActivityContract.View {
    private lateinit var mPresenter: ArActivityContract.Presenter
    private lateinit var arFragment: ArFragment
    private var andyRenderable: ModelRenderable? = null
    private var andy: TransformableNode? = null
    private var helpStep: Int = 0
    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = ArActivityPresenter(this)

        // Enable AR related functionality on ARCore supported devices only.
        checkArCoreApkAvailability()

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        val name = intent.getStringExtra("name")

        //load 3D object
        ModelRenderable.builder()
                .setSource(this, Uri.parse(name))
                .build()
                .thenAccept { renderable -> andyRenderable = renderable }
                .exceptionally { throwable ->
                    Toast.makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                    null
                }

        //слушатель нажатия на плоскость (точки)
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }
            if (!isFistInitAR(this)) {
                changeHelpScreen()
            }

            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            // Create the transformable andy and add it to the anchor.
            andy = TransformableNode(arFragment.transformationSystem)
            andy!!.setParent(anchorNode)
            andy!!.renderable = andyRenderable
            andy!!.select()
        }

        arFragment.arSceneView.scene.addOnUpdateListener(this) //You can do this anywhere. I do it on activity creation post inflating the fragment

        initView()
    }//onCreate

    private fun initView() {
        skipHelpIcon.setOnClickListener {
            finishStepHelp()
        }
        back.setOnClickListener {
            finish()
        }
        share.setOnClickListener { view ->
            if (dialog == null) {
                createDialog()
            } else {
                dialog?.show()
            }
            getFile(false) {
                startShare(it)
                dialog?.dismiss()
            }
        }
        screenShot.setOnClickListener { view ->
            if (dialog == null) {
                createDialog()
            } else {
                dialog?.show()
            }
            askForPermission()
        }
    }


    private fun createDialog() {
        dialog = android.support.v7.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .show()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(R.layout.dialog_create_screen)
    }


    private fun getFile(flag: Boolean, callback: (file: File) -> Unit) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val handlerThread = HandlerThread("PixelCopier")
        handlerThread.start()

        doAsync {
            val firstBitmap = Bitmap.createBitmap(arFragment.arSceneView.width, arFragment.arSceneView.height, Bitmap.Config.ARGB_8888)
            val secondBitmap = mPresenter.getBitmapFromView(screen)
            PixelCopy.request(arFragment.arSceneView, firstBitmap, { res ->
                if (!this@ArActivity.isDestroyed) {

                    val finishBitmap = mPresenter.overlay(firstBitmap, secondBitmap)
                    Log.d("sss", "105")
                    doAsync {
                        val file = mPresenter.createFileForIntent(flag, finishBitmap, this@ArActivity)
                        Log.d("sss", "107")
                        uiThread {
                            callback(file)
                        }
                    }
                }
            }, Handler(handlerThread.looper))
        }
    }


    override fun onResume() {
        super.onResume()

        // Make sure ARCore is installed and up to date.
        try {
            if (mSession == null) {

                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED ->
                        // Success, create the AR session.
                        mSession = Session(this)

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
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            setResult(Activity.RESULT_OK, intent)
            finish()
            return
        } catch (e: Exception) {  // Current catch statements.
            Log.d("Exception", e.printStackTrace().toString())
            return  // mSession is still null.
        }
    }

    private fun checkArCoreApkAvailability() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({ checkArCoreApkAvailability() }, 200)
        }
        if (!availability.isSupported) {
            Toast.makeText(this, getString(R.string.device_unsupported), Toast.LENGTH_LONG).show()
        }
    }

    override fun onUpdate(frameTime: FrameTime) {
        //get the frame from the scene for shorthand
        val frame = arFragment.arSceneView.arFrame
        if (frame != null) {

            if (!isFistInitAR(this)) {

                if (helpStep == 0) {
                    helpStep = HELP_1
                    changeHelpScreen()
                }

                if (helpStep == HELP_2) {
                    //get the trackables to ensure planes are detected
                    val iterator = frame.getUpdatedTrackables(Plane::class.java).iterator()
                    while (iterator.hasNext()) {
                        val plane = iterator.next()
                        if (plane.trackingState == TrackingState.TRACKING) {
                            //появились точки
                            changeHelpScreen()
                        }
                    }
                }

                if (andy != null) {
                    if (andy!!.translationController.isTransforming && helpStep == HELP_4) {
                        changeHelpScreen()
                    }

                    if (andy!!.rotationController.isTransforming && helpStep == HELP_5) {
                        changeHelpScreen()
                    }

                    if (andy!!.scaleController.isTransforming && helpStep == HELP_6) {
                        changeHelpScreen()
                    }
                }
            } else {
                back.visibility = View.VISIBLE
                logoTextImage.visibility = View.VISIBLE
                footer.visibility = View.VISIBLE
            }
        }
    }

    private fun changeHelpScreen() {
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
        helpStep++
    }

    private fun finishStepHelp() {
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
            startActivity(Intent.createChooser(intent, "Share image via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun askForPermission() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE);
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getFile(true) {
                        dialog?.dismiss()
                        Toast.makeText(this, "Скриншот сохранен", Toast.LENGTH_LONG).show()
                    }
                } else {
                    finish()
                }
                return
            }
        }
    }
}
