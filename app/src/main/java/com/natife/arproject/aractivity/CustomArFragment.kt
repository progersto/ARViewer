package com.natife.arproject.aractivity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.natife.arproject.entity.ObjectForList
import com.natife.arproject.R
import com.natife.arproject.di.CreatorObjectsModule
import com.natife.arproject.di.DaggerCreatorObjectsComponent
import com.natife.arproject.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast

class CustomArFragment : ArFragment(), Scene.OnUpdateListener, OnCreator, ArActivityContract.View, OnView.OnFragmentDo {
    private lateinit var mPresenter: ArActivityContract.Presenter
    private var flagSession: Boolean = false
    private lateinit var creatorObjects: CreatorObjects
    private var hitResult: HitResult? = null
    private var plane: Plane? = null
    private lateinit var newAnchor: Anchor
    private var save = false
    private var connection = true
    private var oldObjectCreated = false
    private var onCreator: OnCreator = this
    private var cloudAnchor: Anchor? = null
    private var appAnchorState = AppAnchorState.NONE
    private lateinit var image: ImageView
    private lateinit var listNode: MutableList<ObjectForList>
    private var name: String = ""
    private var link: String = ""
    private var resImage: Int = 0
    private var flagLoadNodelist: Boolean = false
    private var helpStep: Int = 0
    private var containerChild: TransformableNode? = null
    private var currentContainer: TransformableNode? = null
    private var countObjList: Int = 0
    private lateinit var onView: OnView.OnActivityDo


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onView = context as OnView.OnActivityDo
    }

    override fun getSessionConfiguration(session: Session): Config {
        planeDiscoveryController.setInstructionView(null)
        val config = super.getSessionConfiguration(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED

        return config
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = ArActivityPresenter(this)
        listNode = mPresenter.getListNode()

        creatorObjects = DaggerCreatorObjectsComponent.builder()
                .creatorObjectsModule(CreatorObjectsModule(onCreator, this))
                .build().getCreatorObjectsModule()

        if (listNode.size > 0) {
            flagLoadNodelist = true
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.intent?.getStringExtra("name")?.let {
            name = it
        }
        activity?.intent?.getStringExtra("link")?.let {
            link = it
        }
        activity?.intent?.getIntExtra("resImage", 0)?.let {
            resImage = it
        }
        if (resImage != 0) {
            create2DObj(resImage) //load 2D object
        } else {
            create3DObj()  //load 3D object
        }//if
    }

    override fun onResume() {
        super.onResume()
        flagSession = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        planeDiscoveryController.hide()  // Hide initial hand gesture
    }


    override fun onUpdate(frameTime: FrameTime) {
        //get the frame from the scene for shorthand
        val frame = arSceneView.arFrame
        if (frame != null) {
            checkUpdatedAnchor()

            val cameraTrackingState = arSceneView.arFrame?.camera?.trackingState
            if (cameraTrackingState == TrackingState.TRACKING && flagSession) {
                flagSession = false
                getCloudAncor()
            }

            if (!isFistInitAR(context!!)) {

                if (helpStep == 0) {
                    helpStep = HELP_1
                    helpStep = onView.changeHelpScreen(helpStep)
                }

                if (helpStep == HELP_2) {
                    //get the trackables to ensure planes are detected
                    val iterator = frame.getUpdatedTrackables(Plane::class.java).iterator()
                    while (iterator.hasNext()) {
                        val plane = iterator.next()
                        if (plane.trackingState == TrackingState.TRACKING) {
                            //the plane is detected
                            helpStep = onView.changeHelpScreen(helpStep)
                        }
                    }
                }

                if (containerChild != null) {
                    if (containerChild!!.translationController.isTransforming && (helpStep == HELP_4
                                    || helpStep == HELP_5 || helpStep == HELP_6)) {
                        helpStep = onView.changeHelpScreen(helpStep)
                    }
                }
            } else {
                onView.notSetHelp()
            }
        }
    }


    private fun getCloudAncor() {
        if (flagLoadNodelist) {
            oldObjectCreated = true
            val cloudAnchorId = listNode[countObjList].cloudAnchorId
            val resolvedAnchor = arSceneView.session?.resolveCloudAnchor(cloudAnchorId)
            if (cloudAnchor != null) {
                cloudAnchor = null
            }
            setCloudAnchor(resolvedAnchor)//set cloudAnchor for RESOLVING
            appAnchorState = AppAnchorState.RESOLVING
        }
    }

    private fun checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return
        }
        if (cloudAnchor != null) {
            cloudAnchor?.also {
                val cloudState = it.cloudAnchorState
                if (checkConnection(context!!)) {
                    connection = true
                    if (appAnchorState == AppAnchorState.HOSTING) {
                        hosting(cloudState)
                    } else if (appAnchorState == AppAnchorState.RESOLVING) {
                        resolving(cloudState)
                    }
                } else {
                    if (connection) {
                        longToast(resources.getString(R.string.no_internet))
                        connection = false
                    }
                }
            }
        }
    }

    //save
    private fun hosting(cloudSt: Anchor.CloudAnchorState?) {
        cloudSt?.also { cloudState ->
            when {
                cloudState.isError -> {
                    toast(resources.getString(R.string.save_error))
                    appAnchorState = AppAnchorState.NONE
                    save = false
                    progressBar.visibility = View.GONE
                }
                cloudState == Anchor.CloudAnchorState.SUCCESS -> {
                    val cloudAnchorId = cloudAnchor?.cloudAnchorId//get long id code anchor
                    listNode.add(ObjectForList(cloudAnchorId, name, link, resImage, currentContainer!!.localScale.x))//save data object
                    currentContainer = null
                    toast(resources.getString(R.string.obj_saved))
                    appAnchorState = AppAnchorState.NONE
                    save = false
                    onView.progressBar(View.GONE)
                }
                cloudState == Anchor.CloudAnchorState.TASK_IN_PROGRESS && !save -> {
                    longToast(resources.getString(R.string.save_anchor))
                    onView.progressBar(View.VISIBLE)
                    save = true
                }
            }
        }
    }

    //recovery
    private fun resolving(cloudState: Anchor.CloudAnchorState) {
        if (cloudState == Anchor.CloudAnchorState.ERROR_RESOLVING_LOCALIZATION_NO_MATCH) {
            toast(resources.getString(R.string.recovery_error))
            getCloudAncor()
        } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            if (oldObjectCreated) {
                oldObjectCreated = false
                createOldObject()
            }
        } else if (cloudState == Anchor.CloudAnchorState.TASK_IN_PROGRESS) {
            onView.progressBar(View.VISIBLE)
        }
    }

    private fun createOldObject() {
        if (listNode[countObjList].resImage == 0) {
            creatorObjects.createModelRenderable(listNode[countObjList].link)
        } else {
            create2D(listNode[countObjList].resImage, null, null, cloudAnchor)
        }
    }

    private fun finishCreateOldObj() {
        onView.progressBar(View.GONE)
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

    private fun create3DObj() {
        setOnTapArPlaneListener { hitResult: HitResult, _: Plane, _: MotionEvent ->
            if (!isFistInitAR(context!!)) {
                helpStep = onView.changeHelpScreen(helpStep)
            }

            if (appAnchorState != AppAnchorState.NONE) {
                return@setOnTapArPlaneListener
            }
            val anchor3D = hitResult.createAnchor()
            newAnchor = arSceneView.session!!.hostCloudAnchor(anchor3D)
            setCloudAnchor(newAnchor)//set cloudAnchor for HOSTING
            appAnchorState = AppAnchorState.HOSTING

            //create object
            creatorObjects.createModelRenderable(link)
        }//OnTapArPlaneListener
    }//create3DObj

    private fun create2D(resImage: Int, hitResult: HitResult?, plane: Plane?, resolvedAnchor: Anchor?) {
        this.hitResult = hitResult
        this.plane = plane

        // Create the Anchor Parent
        val anchorNodeParent = createAnchorParent(hitResult, resolvedAnchor, arSceneView)
        // Create the Anchor Child
        createAnchorChild(hitResult, resolvedAnchor, arSceneView, this)

        val view2d: View = LayoutInflater.from(context).inflate(R.layout.temp, null, false)
        image = view2d.findViewById(R.id.image)
        Glide.with(this).load(resImage).apply(RequestOptions().fitCenter()).into(image)

        //create object from View
        creatorObjects.createViewRenderable(view2d, anchorNodeParent)
    }


    private fun create2DObj(resImage: Int) {
        setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            create2D(resImage, hitResult, plane, null)
        }
    }//create2DObj

    private fun setCloudAnchor(newAnchor: Anchor?) {
        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
    }

    override fun thenAcceptModel(transformableNode: TransformableNode) {
        val obj3D = transformableNode
        when (appAnchorState) {
            AppAnchorState.RESOLVING -> {
                val anchorNode = AnchorNode(cloudAnchor)//set anchor from list
                obj3D.setParent(anchorNode)
                val currentScale = listNode[countObjList].currentScale
                obj3D.localScale = Vector3(currentScale, currentScale, currentScale)//set current scale object
                arSceneView.scene.addChild(anchorNode)
                finishCreateOldObj()
            }
            AppAnchorState.HOSTING -> {
                val anchorNode = AnchorNode(newAnchor)
                anchorNode.setParent(arSceneView.scene)
                obj3D.setParent(anchorNode)
                currentContainer = obj3D
            }
        }
    }

    override fun thenAcceptView(transformableNode: TransformableNode, anchorNodeParent: AnchorNode) {
        containerChild = transformableNode

        //put the view on the plane
        var yAxis: FloatArray? = null
        if (hitResult != null) {
            plane?.also { plane -> yAxis = plane.centerPose.yAxis }
        } else {
            val pose = anchorNodeParent.anchor?.pose
            yAxis = pose?.yAxis
            finishCreateOldObj()
        }
        yAxis?.also {
            val planeNormal = Vector3(yAxis!![0], yAxis!![1], yAxis!![2])
            val upQuat: Quaternion = Quaternion.lookRotation(planeNormal, Vector3.up())
            containerChild?.worldRotation = upQuat
            currentContainer = containerChild
        }
    }

    override fun exceptionally() {
        Toast.makeText(context, getString(R.string.unable_load_renderable), Toast.LENGTH_LONG).show()
    }


    private fun createAnchorChild(hitResult: HitResult?, resolvedAnchor: Anchor?,
                                  arSceneView: ArSceneView, fragment: CustomArFragment) {
        val anchorNodeChild: AnchorNode
        if (hitResult != null) {
            val anchorChild = hitResult.createAnchor()
            val newAnchor = fragment.arSceneView.session?.hostCloudAnchor(anchorChild)
            setCloudAnchor(newAnchor)//set cloudAnchor for HOSTING
            appAnchorState = AppAnchorState.HOSTING
            anchorNodeChild = AnchorNode(anchorChild)
        } else {
            anchorNodeChild = AnchorNode(resolvedAnchor)
        }
        anchorNodeChild.setParent(arSceneView.scene)
    }

    private fun createAnchorParent(hitResult: HitResult?, resolvedAnchor: Anchor?, arSceneView: ArSceneView): AnchorNode {
        // Create the Anchor Parent
        val anchorNodeParent = if (hitResult != null) {
            val anchorParent = hitResult.createAnchor()
            AnchorNode(anchorParent)
        } else {
            AnchorNode(resolvedAnchor)
        }
        anchorNodeParent.setParent(arSceneView.scene)
        return anchorNodeParent
    }

    override fun showPlane() {
        //скрываем точки показа плоскости
        this.arSceneView.planeRenderer.isEnabled = !this.arSceneView.planeRenderer.isEnabled
    }
}