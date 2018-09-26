package com.natife.arproject.main

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
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

class ArActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private lateinit var arFragment: ArFragment
    private var andyRenderable: ModelRenderable? = null
    private var andy: TransformableNode? = null
    private var helpStep: Int = 0
    private var mUserRequestedInstall = true
    private var mSession: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable AR related functionality on ARCore supported devices only.
        maybeEnableArButton()

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        val name = intent.getStringExtra("name")

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, Uri.parse(name))
                //                .setSource(this, Uri.parse("busterDrone.sfb"))
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept { renderable -> andyRenderable = renderable }
                .exceptionally { throwable ->
                    val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
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

        if (!isFistInitAR(this)) {
            helpStep = HELP_1
            changeHelpScreen()
        } else {
            back.visibility = View.VISIBLE
            logoTextImage.visibility = View.VISIBLE
            footer.visibility = View.VISIBLE
        }
    }//onCreate


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
                        mUserRequestedInstall = false;
                        return
                    }
                    null -> {
                        finish()
                        Log.d("", "Null in enum argument")
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
            return;
        } catch (e: Exception) {  // Current catch statements.
            Log.d("Exception", e.printStackTrace().toString())
            return;  // mSession is still null.
        }

    }

    private fun maybeEnableArButton() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({ maybeEnableArButton() }, 200)
        }
        if (!availability.isSupported) {
            Toast.makeText(this, getString(R.string.device_unsupported), Toast.LENGTH_LONG).show()
        }
    }

    override fun onUpdate(frameTime: FrameTime) {
        //get the frame from the scene for shorthand
        val frame = arFragment.arSceneView.arFrame
        if (frame != null) {

            if (!isFistInitAR(this) && helpStep == HELP_2) {
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

            if (andy != null && !isFistInitAR(this)) {
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
                skipHelpText.visibility = View.GONE
                skipHelpIcon.visibility = View.GONE
                textHead.visibility = View.GONE
                textBody.visibility = View.GONE
                imageNextDoIt.visibility = View.GONE
                back.visibility = View.VISIBLE
                logoTextImage.visibility = View.VISIBLE
                footer.visibility = View.VISIBLE
                return
            }
        }
        helpStep++
    }

}
