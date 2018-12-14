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
import com.google.sceneform_assets.h
import com.natife.arproject.ObjectForList
import com.natife.arproject.R
import com.natife.arproject.di.CreatorObjectsModule
import com.natife.arproject.di.DaggerCreatorObjectsComponent
import com.natife.arproject.di.DaggerDataBaseComponent
import com.natife.arproject.di.DatabaseModule

import com.natife.arproject.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.File

class ArActivity : AppCompatActivity(), Scene.OnUpdateListener, ArActivityContract.View, OnFragmentReady, OnCreator {

    private lateinit var mPresenter: ArActivityContract.Presenter
    private lateinit var arSceneView: ArSceneView
    private var objChild: TransformableNode? = null
    private var currentObj: TransformableNode? = null
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
    private var appAnchorState = AppAnchorState.NONE
    private var resImage: Int = 0
    private var save = false
    private var connection = true
    private var oldObjectCreated = false
    private var minScale: Float = 0.25f
    private var maxScale: Float = 2f
    private var onCreator: OnCreator = this
    private lateinit var creatorObjects: CreatorObjects
    private lateinit var anchorNodeParent: AnchorNode
    private var hitResult: HitResult? = null
    private var plane: Plane? = null
    private lateinit var newAnchor: Anchor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = ArActivityPresenter(this)
        listNode = mPresenter.getListNode()
        creatorObjects = DaggerCreatorObjectsComponent.builder()
                .creatorObjectsModule(CreatorObjectsModule(this, onCreator))
                .build().getCreatorObjectsModule()

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
                appAnchorState = AppAnchorState.NONE
                save = false
                progressBar.visibility = View.GONE
            }
            cloudState == Anchor.CloudAnchorState.SUCCESS -> {
                val cloudAnchorId = cloudAnchor?.cloudAnchorId//get long id code anchor
                listNode.add(ObjectForList(cloudAnchorId, name, resImage, currentObj!!.localScale.x))//save data object
                currentObj = null
                toast("Объект сохранен")
                appAnchorState = AppAnchorState.NONE
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
        if (cloudState == Anchor.CloudAnchorState.ERROR_RESOLVING_LOCALIZATION_NO_MATCH) {
            toast("Подойдите ближе к точке восстановления объекта и наведите на нее камеру")
            getCloudAncor()
        } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            if (oldObjectCreated) {
                oldObjectCreated = false
                createOldObject()
            }
        } else if (cloudState == Anchor.CloudAnchorState.TASK_IN_PROGRESS) {
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun getCloudAncor() {
        if (flagLoadNodelist) {
            oldObjectCreated = true
            val cloudAnchorId = listNode[countObjList].cloudAnchorId
            val resolvedAnchor = arSceneView.session.resolveCloudAnchor(cloudAnchorId)
            if (cloudAnchor != null) {
                cloudAnchor = null
            }
            setCloudAnchor(resolvedAnchor)//set cloudAnchor for RESOLVING
            appAnchorState = AppAnchorState.RESOLVING
        }
    }

    private fun createOldObject() {
        if (listNode[countObjList].resImage == 0) {
            creatorObjects.createModelRenderable(listNode[countObjList].name)
        } else {
            create2D(listNode[countObjList].resImage, null, null, cloudAnchor)
        }
    }

    private fun finishCreateOldObj() {
        progressBar.visibility = View.GONE
        toast("Объект ${listNode[countObjList].name} восстановлен")
        appAnchorState = AppAnchorState.NONE
        countObjList++
        if (listNode.size > countObjList) {
            getCloudAncor()
        } else {
            flagLoadNodelist = false
        }
        oldObjectCreated = true
    }

    private fun create3DObj(name: String) {
        fragment.setOnTapArPlaneListener { hitResult: HitResult, _: Plane, _: MotionEvent ->
            if (!isFistInitAR(this)) {
                changeHelpScreen()
            }

            if (appAnchorState != AppAnchorState.NONE) {
                return@setOnTapArPlaneListener
            }
            val anchor3D = hitResult.createAnchor()
            newAnchor = fragment.arSceneView.session.hostCloudAnchor(anchor3D)
            setCloudAnchor(newAnchor)//set cloudAnchor for HOSTING
            appAnchorState = AppAnchorState.HOSTING

            //create object
            creatorObjects.createModelRenderable(name)
        }//OnTapArPlaneListener
    }//create3DObj

    private fun create2D(resImage: Int, hitResult: HitResult?, plane: Plane?, resolvedAnchor: Anchor?) {
        this.hitResult = hitResult
        this.plane = plane

        // Create the Anchor Parent
        anchorNodeParent = mPresenter.createAnchorParent(hitResult, resolvedAnchor, arSceneView)
//        //create empty obj for parent
        val objParent = mPresenter.createObjParent(fragment, anchorNodeParent)
        // Create the Anchor Child
        mPresenter.createAnchorChild(hitResult, resolvedAnchor, arSceneView, fragment)

        //create obj child
        if (objChild != null) {
            objChild = null
        }
        objChild = TransformableNode(fragment.transformationSystem)
        objChild?.rotationController?.rotationRateDegrees = 0f//rotation prohibition

        val view2d: View = LayoutInflater.from(this).inflate(R.layout.temp, null, false)
        image = view2d.findViewById(R.id.image)

        Glide.with(this).load(resImage).apply(RequestOptions().fitCenter()).into(image)

        //create object from View
        creatorObjects.createViewRenderable(view2d)

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
                getCloudAncor()
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


    override fun setCloudAnchor(newAnchor: Anchor?) {
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
    }


    override fun setAnchorState(newAppAnchorState: AppAnchorState) {
        appAnchorState = newAppAnchorState
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

    override fun thenAcceptModel(modelRenderable: ModelRenderable) {
         val obj3D = TransformableNode(fragment.transformationSystem)
        obj3D.renderable = modelRenderable
        obj3D.scaleController.minScale = minScale
        obj3D.scaleController.maxScale = maxScale
        obj3D.select()
        when (appAnchorState) {
            AppAnchorState.RESOLVING -> {
                val anchorNode = AnchorNode(cloudAnchor)//set anchor from list
                obj3D.setParent(anchorNode)
                val currentScale = listNode[countObjList].currentScale
                obj3D.localScale = Vector3(currentScale, currentScale, currentScale)//set current scale object
                fragment.arSceneView.scene.addChild(anchorNode)
                finishCreateOldObj()
            }
            AppAnchorState.HOSTING -> {
                val anchorNode = AnchorNode(newAnchor)
                anchorNode.setParent(arSceneView.scene)
                obj3D.setParent(anchorNode)
                currentObj = obj3D
            }
        }
    }

    override fun thenAcceptView(viewRenderable: ViewRenderable) {
        objChild!!.renderable = viewRenderable
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
            objChild!!.scaleController.minScale = minScale
            objChild!!.scaleController.maxScale = maxScale
            currentObj = objChild
        }
        if (hitResult == null) {
            finishCreateOldObj()
        }
    }

    override fun exceptionally() {
        makeText(this, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
    }
}
