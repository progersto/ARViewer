package com.natife.arproject.aractivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.renderscript.AllocationAdapter.create2D
import android.support.design.widget.Snackbar
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
import android.widget.Toast.makeText
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import com.natife.arproject.aractivity.ArActivity.AppAnchorState.NONE
import com.natife.arproject.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.File

class ArActivity : AppCompatActivity(), Scene.OnUpdateListener, ArActivityContract.View, OnFragmentReady {
    private lateinit var mPresenter: ArActivityContract.Presenter
    private lateinit var arSceneView: ArSceneView
    private var objChild: TransformableNode? = null
    private var objParent: TransformableNode? = null
    private var helpStep: Int = 0
    private var mUserRequestedInstall = true
    private var dialog: AlertDialog? = null
    private lateinit var image: ImageView
    private lateinit var listNode: MutableList<ObjectForList>
    private var name: String = ""
    private var flagLoadNodelist: Boolean = false
    private var flagSession: Boolean = false
    private var countObjList: Int = 0
    private lateinit var fragment: CustomArFragment
    private var cloudAnchor: Anchor? = null
    private var appAnchorState = NONE
    private var resImage: Int = 0
    private var save = false
    private var connection = true

    private enum class AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = ArActivityPresenter(this)
        listNode = mPresenter.getListNode()

        // Enable AR related functionality on ARCore supported devices only.
        checkArCoreApkAvailability()

        fragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as CustomArFragment
        fragment.planeDiscoveryController.hide()  // Hide initial hand gesture
        arSceneView = fragment.arSceneView


        name = intent.getStringExtra("name")
        resImage = intent.getIntExtra("resImage", 0)

        if (resImage != 0) {
            create2DObj(resImage) //load 2D object
        } else {
            create3DObj(name)  //load 3D object
        }//if
        if (listNode.size > 0) {
            flagLoadNodelist = true
        }
        arSceneView.scene.addOnUpdateListener(this) //You can do this anywhere. I do it on activity creation post inflating the fragment
        initView()
    }//onCreate


    private fun checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return
        }
        if (cloudAnchor != null) {
            val cloudState = cloudAnchor!!.cloudAnchorState

            if (checkConnection(this)) {
                connection = true
                if (appAnchorState == AppAnchorState.HOSTING) {
                    hosting(cloudState)
                } else if (appAnchorState == AppAnchorState.RESOLVING) {
                    resolving(cloudState)
                }
            } else {
                if (connection) {
                    longToast("Отсутствует интернет, сохранение позиции невозможно")
                    connection = false
                }
            }
        }
    }

    private fun hosting(cloudState: Anchor.CloudAnchorState) {
        when {
            cloudState.isError -> {
                toast("Ошибка сохранения. Подойдите ближе к объекту и наведите на него камеру")
                appAnchorState = NONE
                save = false
                progressBar.visibility = View.GONE
            }
            cloudState == Anchor.CloudAnchorState.SUCCESS -> {
                val cloudAnchorId = cloudAnchor?.cloudAnchorId//get long id code anchor
                listNode.add(ObjectForList(cloudAnchorId, name, resImage))//save anchor
                toast("Объект сохранен")
                appAnchorState = NONE
                save = false
                progressBar.visibility = View.GONE
            }
            cloudState == Anchor.CloudAnchorState.TASK_IN_PROGRESS && !save -> {
                longToast("Сохранение позиции...")
                progressBar.visibility = View.VISIBLE
                save = true
            }
        }
    }

    private fun resolving(cloudState: Anchor.CloudAnchorState) {
        if (cloudState.isError) {
            toast("Подойдите ближе к точке восстановления объекта и наведите на нее камеру")
            appAnchorState = NONE
            progressBar.visibility = View.GONE
        } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            progressBar.visibility = View.GONE
            toast("Объект ${listNode[countObjList].name} восстановлен")
            appAnchorState = NONE
            countObjList++
            if (listNode.size > countObjList) {
                createOldObj()
            } else {
                flagLoadNodelist = false
            }
        } else if (cloudState == Anchor.CloudAnchorState.TASK_IN_PROGRESS) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun createOldObj() {
        if (flagLoadNodelist) {
            val cloudAnchorId = listNode[countObjList].cloudAnchorId
            val resolvedAnchor = arSceneView.session.resolveCloudAnchor(cloudAnchorId)
            setCloudAnchor(resolvedAnchor)//set cloudAnchor for RESOLVING
            appAnchorState = AppAnchorState.RESOLVING

            if (listNode[countObjList].resImage == 0) {
                ModelRenderable.builder()
                        .setSource(this, Uri.parse(listNode[countObjList].name))
                        .build()
                        .thenAccept { renderable ->
                            val anchorNode = AnchorNode(resolvedAnchor)//set anchor from list
                            val obj3D = TransformableNode(fragment.transformationSystem)
                            obj3D.renderable = renderable
                            obj3D.setParent(anchorNode)
                            fragment.arSceneView.scene.addChild(anchorNode)
                            obj3D.select()
                        }
                        .exceptionally {
                            makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                            null
                        }
            } else {
                create2D(listNode[countObjList].resImage, null, null, resolvedAnchor)
            }
        }
    }

    private fun create3DObj(name: String) {
        fragment.setOnTapArPlaneListener { hitResult: HitResult, _: Plane, _: MotionEvent ->
            if (!isFistInitAR(this)) {
                changeHelpScreen()
            }

            if (appAnchorState != NONE) {
                return@setOnTapArPlaneListener
            }
            val anchor3D = hitResult.createAnchor()
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(anchor3D)
            setCloudAnchor(newAnchor)//set cloudAnchor for HOSTING
            appAnchorState = AppAnchorState.HOSTING
            val anchorNode = AnchorNode(newAnchor)
            anchorNode.setParent(arSceneView.scene)

            //create object
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(name))
                    .build()
                    .thenAccept { renderable ->
                        // Create the transformable object and add it to the anchor.
                        val obj3D = TransformableNode(fragment.transformationSystem)
                        obj3D.setParent(anchorNode)
                        obj3D.renderable = renderable
                        obj3D.name = name
                        obj3D.select()
                        obj3D.setOnTapListener { _, _ ->
                            Log.d("sss", "setOnTapListener")
                        }
                    }
                    .exceptionally {
                        makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                        null
                    }
        }//OnTapArPlaneListener
    }//create3DObj

    private fun create2D(resImage: Int, hitResult: HitResult?, plane: Plane?, resolvedAnchor: Anchor?) {
        // Create the Anchor Parent
        val anchorNodeParent = if (hitResult != null) {
            val anchorParent = hitResult.createAnchor()
            AnchorNode(anchorParent)
        } else {
            AnchorNode(resolvedAnchor)
        }
        anchorNodeParent.setParent(arSceneView.scene)

        //create empty obj for parent
        if (objParent != null) {
            objParent = null
        }
        objParent = TransformableNode(fragment.transformationSystem)
        objParent?.setParent(anchorNodeParent)
        objParent?.select()

        // Create the Anchor Child
        val anchorNodeChild: AnchorNode
        if (hitResult != null) {
            val anchorChild = hitResult.createAnchor()
            val newAnchor = fragment.arSceneView.session.hostCloudAnchor(anchorChild)
            setCloudAnchor(newAnchor)//set cloudAnchor for HOSTING
            appAnchorState = AppAnchorState.HOSTING
            anchorNodeChild = AnchorNode(anchorChild)
        } else {
            anchorNodeChild = AnchorNode(resolvedAnchor)
        }
        anchorNodeChild.setParent(arSceneView.scene)

        if (objChild != null) {
            objChild = null
        }
        objChild = TransformableNode(fragment.transformationSystem)
        objChild?.rotationController?.rotationRateDegrees = 0f//запрет вращения

        val view2d: View = LayoutInflater.from(this).inflate(R.layout.temp, null, false)
        image = view2d.findViewById(R.id.image)

        Glide.with(this).load(resImage).apply(RequestOptions().fitCenter()).into(image)

        //create object from View
        ViewRenderable.builder()
                .setView(this, view2d)
                .build()
                .thenAccept { renderable ->
                    objChild!!.renderable = renderable
                    // Change the rotation
                    var yAxis: FloatArray? = null
                    if (hitResult != null) {
                        plane?.also { plane -> yAxis = plane.centerPose.yAxis }
                    } else {
                        val pose = anchorNodeParent.anchor.pose
                        yAxis = pose.yAxis
                    }
                    yAxis?.also {
                        val planeNormal = Vector3(yAxis!![0], yAxis!![1], yAxis!![2])
                        val upQuat: Quaternion = Quaternion.lookRotation(planeNormal, Vector3.up())
                        objChild?.worldRotation = upQuat
                    }

                }
                .exceptionally {
                    makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
                    null
                }
        objChild?.setParent(objParent)
        objChild?.setOnTouchListener { _, _ -> objParent!!.select() }
    }


    private fun create2DObj(resImage: Int) {
        fragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            create2D(resImage, hitResult, plane, null)
        }
    }//create2DObj


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
            PixelCopy.request(arSceneView, firstBitmap, {
                if (!this@ArActivity.isDestroyed) {

                    val finishBitmap = mPresenter.overlay(firstBitmap, secondBitmap)
                    doAsync {
                        val file = mPresenter.createFileForIntent(flag, finishBitmap, this@ArActivity)
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


    override fun onUpdate(frameTime: FrameTime) {
        //get the frame from the scene for shorthand
        val frame = arSceneView.arFrame
        if (frame != null) {
            checkUpdatedAnchor()

            val cameraTrackingState = fragment.arSceneView.arFrame.camera.trackingState
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
                            //the plane is detected
                            changeHelpScreen()
                        }
                    }
                }

                if (objChild != null) {
                    if (objChild!!.translationController.isTransforming && (helpStep == HELP_4
                                    || helpStep == HELP_5 || helpStep == HELP_6)) {
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


    private fun setCloudAnchor(newAnchor: Anchor?) {
        cloudAnchor = newAnchor
        appAnchorState = NONE
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
                        makeText(this, "Скриншот сохранен", Toast.LENGTH_LONG).show()
                    }
                } else {
                    finish()
                }
                return
            }
        }
    }


    override fun fragmentReady() {
        flagSession = true
    }
}
