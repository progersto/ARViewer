package com.natife.arproject.aractivity;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.natife.arproject.R;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable AR related functionality on ARCore supported devices only.
        maybeEnableArButton();


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        String name = getIntent().getStringExtra("name");

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, Uri.parse(name))
//                .setSource(this, Uri.parse("busterDrone.sfb"))
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });


//        Anchor anchor = plane.createAnchor(plane.getCenterPose());
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//        MaterialFactory
//                .makeTransparentWithColor(this, new Color(1, .5f, .5f, .5f))
//                .thenAccept(color -> {
//                    Renderable renderable = ShapeFactory.makeCube(
//                            new Vector3(0.42f, 0.0001f, 0.24f),
//                            new Vector3(-0.22f, 0, -0.10f), color);
//
//                    Node node = new Node();
//                    node.setRenderable(renderable);
//                    node.setParent(anchorNode);
//                });




        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.select();
                });


        arFragment.getArSceneView().getScene().addOnUpdateListener(this); //You can do this anywhere. I do it on activity creation post inflating the fragment
    }//onCreate

    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            Toast.makeText(this, "supported", Toast.LENGTH_LONG).show();
            // indicator on the button.
        } else { // Unsupported or unknown.
            Toast.makeText(this, "Unsupported or unknown.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        //get the frame from the scene for shorthand
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame != null) {
            //get the trackables to ensure planes are detected
            Iterator<Plane> var3 = frame.getUpdatedTrackables(Plane.class).iterator();
            while (var3.hasNext()) {
                Plane plane = var3.next();
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    Log.d("ddd", "TRACKING");
                }
            }
        }
    }
}
