package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.azure.spatialanchors.AnchorLocateCriteria;
import com.microsoft.azure.spatialanchors.AnchorLocatedEvent;
import com.microsoft.azure.spatialanchors.CloudSpatialAnchorSession;
import com.microsoft.azure.spatialanchors.LocateAnchorStatus;
import com.microsoft.azure.spatialanchors.SessionLogLevel;


import com.microsoft.CloudServices;

import java.util.ArrayList;
import java.util.List;

public class NewActivity extends AppCompatActivity {
    private CloudSpatialAnchorSession cloudSession;
    private boolean sessionInitialized = false;
    private TextView logTextView;

    private ArSceneView newSceneView;

    private ArFragment newArFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        this.newArFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.new_ar_fragment);
        this.logTextView = findViewById(R.id.logTextView);

        if (newArFragment == null) {
            // Handle the case where the fragment is not found
            Log.e("NewActivity", "ArFragment not found in layout.");
            return;
        }

        this.newSceneView = newArFragment.getArSceneView();
        Scene sScene = newSceneView.getScene();
        sScene.addOnUpdateListener(frameTime -> {
            if (this.cloudSession != null) {
                this.cloudSession.processFrame(newSceneView.getArFrame());
            }
        });
        sScene.addOnUpdateListener(this::scene_OnUpdate);

        //initializeSession();

        //fetchAndLocateAnchors();

        initializeSession();
    }


    private void scene_OnUpdate(FrameTime frameTime) {
        if (!sessionInitialized) {
            //retry if initializeSession did an early return due to ARCore Session not yet available (i.e. sceneView.getSession() == null)
            initializeSession();
        }
    }

    private void initializeSession() {
        if (newArFragment == null) {
            logToTextView("ArFragment not found in layout.");
            return;
        }

        newSceneView = newArFragment.getArSceneView();
        if (newSceneView == null) {
            logToTextView("ArSceneView not found in ArFragment.");
            return;
        }

        if (newSceneView.getSession() == null) {
            logToTextView("ARCore Session is not ready.");
            return;
        }

        // Proceed with session initialization
        cloudSession = new CloudSpatialAnchorSession();
        cloudSession.setSession(newSceneView.getSession());
        cloudSession.setLogLevel(SessionLogLevel.Information);
        cloudSession.addOnLogDebugListener(args -> Log.d("ASAInfo", args.getMessage()));
        cloudSession.addErrorListener(args -> Log.e("ASAError", String.format("%s: %s", args.getErrorCode().name(), args.getErrorMessage())));

        sessionInitialized = true;

        cloudSession.addSessionUpdatedListener(args -> {
            // Handle session updated event if needed
        });

        this.cloudSession.getConfiguration().setAccountId("6df5c2c8-b1a8-49eb-8611-8644c64b7540");
        this.cloudSession.getConfiguration().setAccountKey("HhGUM+1mSlVMQLEPuQaxLy3eD0VS4ufIJFuL9nsTENk=");
        this.cloudSession.getConfiguration().setAccountDomain("southeastasia.mixedreality.azure.com");
        cloudSession.start();

        logToTextView("Cloud Spatial Anchor Session started.");
        fetchAndLocateAnchors();
        logToTextView("Fetch and locate anchors function called.");
    }


    private void fetchAndLocateAnchors() {
        logToTextView("Inside fetch and locate anchors function.");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("anchors");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> anchorIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String cloudAnchorId = snapshot.child("cloudAnchorId").getValue(String.class);
                    float distance = snapshot.child("distance").getValue(Float.class);
                    anchorIds.add(cloudAnchorId);
                    logToTextView("Fetched Anchor ID: " + cloudAnchorId);
                }

                // Create a watcher for all fetched anchor IDs
                createWatcherForAnchors(anchorIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to fetch data: " + databaseError.getMessage());
                logToTextView("Failed to fetch data from Firebase: " + databaseError.getMessage());
            }
        });
        logToTextView("Fetching and locating anchors...");
    }

    private void createWatcherForAnchors(List<String> anchorIds) {
        AnchorLocateCriteria criteria = new AnchorLocateCriteria();
        criteria.setIdentifiers(anchorIds.toArray(new String[0]));

        cloudSession.createWatcher(criteria);

        logToTextView("Creating watcher for anchors...");

        this.cloudSession.addAnchorLocatedListener(args -> {
            runOnUiThread(() -> {
                switch (args.getStatus()) {
                    case Located:
                        AnchorNode anchorNode = new AnchorNode();
                        anchorNode.setAnchor(args.getAnchor().getLocalAnchor());
                        MaterialFactory.makeOpaqueWithColor(NewActivity.this, new Color(android.graphics.Color.GREEN))
                                .thenAccept(greenMaterial -> {
                                    Renderable nodeRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.15f, 0.0f), greenMaterial);
                                    anchorNode.setRenderable(nodeRenderable);
                                    anchorNode.setParent(newArFragment.getArSceneView().getScene());
                                });
                        logToTextView("Anchor located: " + args.getAnchor().getIdentifier());
                        break;
                    case AlreadyTracked:
                        logToTextView("Anchor already tracked: " + args.getAnchor().getIdentifier());
                        break;
                    case NotLocatedAnchorDoesNotExist:
                        logToTextView("Anchor does not exist: " + args.getAnchor().getIdentifier());
                        break;
                    case NotLocated:
                        logToTextView("Anchor not located: " + args.getAnchor().getIdentifier());
                        break;
                    default:
                        logToTextView("Unknown anchor status: " + args.getStatus() + ", Anchor ID: " + args.getAnchor().getIdentifier());
                }
            });
        });
    }



    private void logToTextView(String message) {
        Log.d("AppLog", message);
        if (logTextView != null) {
            logTextView.append(message + "\n");
        }
    }
}