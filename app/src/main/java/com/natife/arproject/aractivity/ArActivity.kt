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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3

import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.*
import com.natife.arproject.ObjectForList
import com.natife.arproject.R
import com.natife.arproject.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

class ArActivity : AppCompatActivity(), Scene.OnUpdateListener, ArActivityContract.View, OnFragmentReady {
    private lateinit var mPresenter: ArActivityContract.Presenter
    //    private lateinit var arFragment: ArFragment
    private lateinit var arSceneView: ArSceneView
    private var andyRenderable: ModelRenderable? = null
    private var objChild: TransformableNode? = null
    private var objParent: TransformableNode? = null
    private var helpStep: Int = 0
    private var mUserRequestedInstall = true
    private var mSession: Session? = null
    private var dialog: AlertDialog? = null
    private var view2d: View? = null
    private lateinit var image: ImageView
    private lateinit var listNode: MutableList<ObjectForList>
    private var name: String = ""
    private var flagLoadNodelist: Boolean = false
    private var flagSession: Boolean = false


    private lateinit var fragment: CustomArFragment
    private var cloudAnchor: Anchor? = null

    private enum class AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

    private var appAnchorState = AppAnchorState.NONE

//    private val snackbarHelper = SnackbarHelper()

//    private var storageManager: StorageManager? = null

    private var selectedObject: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = ArActivityPresenter(this)
        listNode = mPresenter.getListNode()

        // Enable AR related functionality on ARCore supported devices only.
        checkArCoreApkAvailability()

//        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        fragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as CustomArFragment
        fragment.planeDiscoveryController.hide()  // Hide initial hand gesture
        arSceneView = fragment.arSceneView

        name = intent.getStringExtra("name")
        val resImage = intent.getIntExtra("resImage", 0)

        if (resImage != 0) {
            create2DObj(resImage) //load 2D object
        } else {
            if (listNode.size > 0) {
                flagLoadNodelist = true
            } else {
                create3DObj(name)  //load 3D object
            }
        }//if
        arSceneView.scene.addOnUpdateListener(this) //You can do this anywhere. I do it on activity creation post inflating the fragment
        initView()
    }//onCreate


    override fun fragmentReady() {
        flagSession = true
    }

    private fun createOldObj() {
        if (flagLoadNodelist) {
            val cloudAnchorId = listNode[0].cloudAnchorId

            var ses = fragment.arSceneView.session
            val resolvedAnchor = ses.resolveCloudAnchor(cloudAnchorId)
            Log.d("sss", "resolvedAnchor = " + resolvedAnchor.toString())
            setCloudAnchor(resolvedAnchor)
            appAnchorState = AppAnchorState.RESOLVING

            ModelRenderable.builder()
                    .setSource(this, Uri.parse(listNode[0].name))
                    .build()
                    .thenAccept { renderable ->
                        val anchorNode = AnchorNode(cloudAnchor)
                        andyRenderable = renderable
                        val objChild1 = TransformableNode(fragment.transformationSystem)
                        objChild1.renderable = andyRenderable
                        objChild1.setParent(anchorNode)
                        fragment.arSceneView.scene.addChild(anchorNode)
                        objChild1.select()
                    }
                    .exceptionally { throwable ->
                        Toast.makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                        null
                    }
            flagLoadNodelist = false
        }
    }


    private fun create3DObj(name: String) {
        //create object
        ModelRenderable.builder()
                .setSource(this, Uri.parse(name))
                .build()
                .thenAccept { renderable -> andyRenderable = renderable }
                .exceptionally { throwable ->
                    Toast.makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                    null
                }


        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }
            if (!isFistInitAR(this)) {
                changeHelpScreen()
            }


            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING || appAnchorState != AppAnchorState.NONE) {
                return@setOnTapArPlaneListener
            }
            val anchor2 = hitResult.createAnchor()
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(anchor2)

            setCloudAnchor(newAnchor)

            appAnchorState = AppAnchorState.HOSTING
//
//            placeObject(fragment, cloudAnchor, selectedObject)


            val anchorNode = AnchorNode(cloudAnchor)
            anchorNode.setParent(arSceneView.scene)

            // Create the transformable object and add it to the anchor.
            objChild = TransformableNode(fragment.transformationSystem)
            objChild?.setParent(anchorNode)
            objChild?.renderable = andyRenderable
            objChild?.name = name
            objChild?.select()
            objChild?.setOnTapListener { hitTestResult, motionEvent ->
                Log.d("sss", "setOnTapListener")
            }
        }//OnTapArPlaneListener
    }//create3DObj


    private fun create2DObj(resImage: Int) {
        view2d = LayoutInflater.from(this).inflate(R.layout.temp, null, false)
        image = view2d!!.findViewById(R.id.image)
        Picasso.get().load(resImage).into(image)

        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            //            if (objParent == null) {  //create only one object

            // Create the Anchor Parent
            val anchorParent = hitResult.createAnchor()
            val anchorNodeParent = AnchorNode(anchorParent)
            anchorNodeParent.setParent(arSceneView.scene)

            //create empty obj for parent
            objParent = TransformableNode(fragment.transformationSystem)
            objParent?.setParent(anchorNodeParent)
            objParent?.select()

            // Create the Anchor Child
            val anchorChild = hitResult.createAnchor()
            val anchorNodeChild = AnchorNode(anchorChild)
            anchorNodeChild.setParent(arSceneView.scene)

            objChild = TransformableNode(fragment.transformationSystem)
            objChild?.rotationController?.rotationRateDegrees = 0f//запрет вращения

            //create object from View
            ViewRenderable.builder()
                    .setView(this, view2d)
                    .build()
                    .thenAccept { renderable ->
                        objChild!!.renderable = renderable
                        // Change the rotation
                        if (plane.type == Plane.Type.VERTICAL) {
                            val yAxis = plane.centerPose.yAxis
                            val planeNormal = Vector3(yAxis[0], yAxis[1], yAxis[2])
                            val upQuat: Quaternion = Quaternion.lookRotation(planeNormal, Vector3.up())
                            objChild?.worldRotation = upQuat
                        } else if (plane.type == Plane.Type.HORIZONTAL_DOWNWARD_FACING) {
                            val yAxis = plane.centerPose.yAxis
                            val planeNormal = Vector3(yAxis[0], yAxis[1], yAxis[2])
                            val upQuat: Quaternion = Quaternion.lookRotation(planeNormal, Vector3.up())
                            objChild?.worldRotation = upQuat
                        } else if (plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                            val yAxis = plane.centerPose.yAxis
                            val planeNormal = Vector3(yAxis[0], yAxis[1], yAxis[2])
                            val upQuat: Quaternion = Quaternion.lookRotation(planeNormal, Vector3.up())
                            objChild?.worldRotation = upQuat
                        }
                    }
                    .exceptionally { throwable ->
                        Toast.makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                        null
                    }
            objChild?.setParent(objParent)
            objChild?.setOnTouchListener { hitTestResult, motionEvent ->
                objParent!!.select()
            }
//            }
        }
    }//create2DObj


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
    }//initView


    private fun createDialog() {
        dialog = android.support.v7.app.AlertDialog.Builder(this)
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
            val firstBitmap = Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
            val secondBitmap = mPresenter.getBitmapFromView(screen)
            PixelCopy.request(arSceneView, firstBitmap, { res ->
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
    }//getFile


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
                // Create default config and check if supported.
                val config = Config(mSession)
                config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
                mSession!!.configure(config)
            } else {
                mSession!!.resume()
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
        val frame = arSceneView.arFrame
        if (frame != null) {
            checkUpdatedAnchor()

            var cameraTrackingState = fragment.arSceneView.arFrame.camera.trackingState
            if (cameraTrackingState == TrackingState.TRACKING && flagSession) {
                flagSession = false
                createOldObj()
            }

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

                if (objChild != null) {
                    if (objChild!!.translationController.isTransforming && helpStep == HELP_4) {
                        changeHelpScreen()
                    }

                    if (objChild!!.rotationController.isTransforming && helpStep == HELP_5) {
                        changeHelpScreen()
                    }

                    if (objChild!!.scaleController.isTransforming && helpStep == HELP_6) {
                        changeHelpScreen()
                    }
                }
            } else {
                if (back.visibility == View.GONE) {
                    back.visibility = View.VISIBLE
                    logoTextImage.visibility = View.VISIBLE
                    footer.visibility = View.VISIBLE
                }
            }
        }
    }

    // private synchronized void checkUpdatedAnchor() {
    private fun checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return
        }
        if (cloudAnchor != null) {
            val cloudState = cloudAnchor!!.cloudAnchorState

            if (appAnchorState == AppAnchorState.HOSTING) {

                if (cloudState.isError) {
//                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor: $cloudState")
                    appAnchorState = AppAnchorState.NONE
                } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                    val cloudAnchorId = cloudAnchor?.cloudAnchorId
                    listNode.add(ObjectForList(cloudAnchorId, name))
//                storageManager.nextShortCode({ shortCode ->
//                    if (shortCode == null) {
//                        snackbarHelper.showMessageWithDismiss(this, "Couldn't get shortcode.")
//                        return@storageManager.nextShortCode
//                    }
//                    storageManager.storeUsingShortCode(shortCode, cloudAnchor.getCloudAnchorId())
//
//                    snackbarHelper.showMessageWithDismiss(this, "Anchor hosted! Cloud Short Code: " + shortCode!!)
//                })
                    Toast.makeText(this, "Объект сохранен", Toast.LENGTH_SHORT).show()
                    appAnchorState = AppAnchorState.HOSTED
                }
            } else if (appAnchorState == AppAnchorState.RESOLVING) {
                if (cloudState.isError) {
                    appAnchorState = AppAnchorState.NONE
                } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                    Toast.makeText(this, "Объект восстановлен", Toast.LENGTH_SHORT).show()
                    appAnchorState = AppAnchorState.RESOLVED
                }
            }
        }
    }


    private fun setCloudAnchor(newAnchor: Anchor?) {
        if (cloudAnchor != null) {
            cloudAnchor!!.detach()
        }

        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
//        snackbarHelper.hide(this)
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
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE)
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

    override fun onPause() {
        super.onPause()
        mSession!!.pause()
    }
}
