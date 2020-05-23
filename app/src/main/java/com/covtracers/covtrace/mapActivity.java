// Created by Covtrace on 2020-05-05.
// Copyright © 2020 Covtrace. All rights reserved.
// This file is subject to the terms and conditions defined in
// file 'LICENSE.txt', which is part of this source code package.

//  mapActivity.java
//  Covtrace


package com.covtracers.covtrace;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Connection;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import ch.uepaa.p2pkit.AlreadyEnabledException;
import ch.uepaa.p2pkit.P2PKit;
import ch.uepaa.p2pkit.P2PKitStatusListener;
import ch.uepaa.p2pkit.StatusResult;
import ch.uepaa.p2pkit.discovery.DiscoveryInfoTooLongException;
import ch.uepaa.p2pkit.discovery.DiscoveryInfoUpdatedTooOftenException;
import ch.uepaa.p2pkit.discovery.DiscoveryListener;
import ch.uepaa.p2pkit.discovery.DiscoveryPowerMode;
import ch.uepaa.p2pkit.discovery.Peer;
import ch.uepaa.p2pkit.discovery.ProximityStrength;

public class mapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String APP_KEY = "4b45a4a433314a4eb20a3742b7168325";
    private final Set<Peer> nearbyPeers = new HashSet<>();
    private GoogleMap mMap;
  
    private static final String TAG = "Map";
//  private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
  
    // Discovery events listener
    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onStateChanged(final int state) {
            handleDiscoveryStateChange(state);
            Log.i("DiscoveryListener", "Discovery state changed: " + state);
        }

        @Override
        public void onPeerDiscovered(final Peer peer) {
            Log.i("DiscoveryListener", "Peer discovered: " + peer.getPeerId() + ". Proximity strength: " + peer.getProximityStrength());
            nearbyPeers.add(peer);
            handlePeerDiscovered(peer);
        }

        @Override
        public void onPeerLost(final Peer peer) {
            Log.i("DiscoveryListener", "Peer lost: " + peer.getPeerId());
            nearbyPeers.remove(peer);
            handlePeerLost(peer);
        }

        @Override
        public void onPeerUpdatedDiscoveryInfo(Peer peer) {
            Log.i("DiscoveryListener", "Peer updated discovery info: " + peer.getPeerId());
            handlePeerUpdatedDiscoveryInfo(peer);
        }

        @Override
        public void onProximityStrengthChanged(Peer peer) {
            Log.i("DiscoveryListener", "Peer changed proximity strength: " + peer.getPeerId() + ". Proximity strength: " + peer.getProximityStrength());
            handlePeerChangedProximityStrength(peer);
        }
    };
    private final P2PKitStatusListener p2pKitStatusListener = new P2PKitStatusListener() {

        @Override
        public void onEnabled() {
            Log.i("P2PKitStatusListener", "Successfully enabled p2pkit");

            UUID ownNodeId = P2PKit.getMyPeerId();
            startDiscovery();
        }

        @Override
        public void onDisabled() {
            Log.i("P2PKitStatusListener", "p2pkit disabled");
        }

        public void onError(StatusResult statusResult) {
            handleStatusResult(statusResult);
            Log.e("P2PKitStatusListener", "p2pkit lifecycle error with code: " + statusResult.getStatusCode());
        }

        @Override
        public void onException(Throwable throwable) {
            String errorMessage = "An error occurred and p2pkit stopped, please try again.";
            // showError(errorMessage, true);
            Log.e("P2PKitStatusListener", "p2pkit threw an exception: " + Log.getStackTraceString(throwable));
            // teardownPeers();
        }
    };

    public void enableP2PKit() {
        try {
            Log.i("P2PKit", "Enabling p2pkit");
            P2PKit.enable(this, APP_KEY, p2pKitStatusListener);
        } catch (AlreadyEnabledException e) {
            Log.w("P2PKit", "p2pkit is already enabled " + e.getLocalizedMessage());
        }
    }

    public void startDiscovery() {
        Log.i("P2PKit", "Start discovery");

        try {
            P2PKit.enableProximityRanging();
            P2PKit.startDiscovery("Hello p2pkit".getBytes(), DiscoveryPowerMode.HIGH_PERFORMANCE, mDiscoveryListener);
        } catch (DiscoveryInfoTooLongException e) {
            Log.w("P2PKit", "Can not start discovery, discovery info is to long " + e.getLocalizedMessage());
        }
    }

    private boolean pushNewDiscoveryInfo(byte[] data) {
        Log.i("P2PKit", "Push new discovery info");
        boolean success = false;

        try {
            P2PKit.pushDiscoveryInfo(data);
            success = true;
        } catch (DiscoveryInfoTooLongException e) {
            Log.e("P2PKit", "Failed to push new discovery info, info too long: " + e.getLocalizedMessage());
        } catch (DiscoveryInfoUpdatedTooOftenException e) {
            Log.e("P2PKit", "Failed to push new discovery info due to throttling: " + e.getLocalizedMessage());
        }

        return success;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableP2PKit();
        setContentView(R.layout.activity_map);
        getLocationPermission();
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.mapActivity);
//        mapFragment.getMapAsync(this);
        Button toDashboard = findViewById(R.id.toDashboard);
        toDashboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mapActivity.this, statusActivity.class);
                startActivity(intent);
            }
        });
    }


    private void handleStatusResult(final StatusResult statusResult) {

        String description = "";

        if (statusResult.getStatusCode() == StatusResult.INVALID_APP_KEY) {
            description = "Invalid app key";
        } else if (statusResult.getStatusCode() == StatusResult.INVALID_APPLICATION_ID) {
            description = "Invalid application ID";
        } else if (statusResult.getStatusCode() == StatusResult.INCOMPATIBLE_CLIENT_VERSION) {
            description = "Incompatible p2pkit (SDK) version, please update";
        } else if (statusResult.getStatusCode() == StatusResult.SERVER_CONNECTION_UNAVAILABLE) {
            description = "Server connection not available";
        }

        // showerror(description, true);
    }

    private void handleDiscoveryStateChange(final int state) {

        if (state == DiscoveryListener.STATE_OFF) {
            return;
        }

        if ((state & DiscoveryListener.STATE_LOCATION_PERMISSION_NOT_GRANTED) == DiscoveryListener.STATE_LOCATION_PERMISSION_NOT_GRANTED) {
            Toast.makeText(this, R.string.p2pkit_state_no_location_permission, Toast.LENGTH_LONG).show();
        } else if ((state & DiscoveryListener.STATE_SERVER_CONNECTION_UNAVAILABLE) == DiscoveryListener.STATE_SERVER_CONNECTION_UNAVAILABLE) {
            Toast.makeText(this, R.string.p2pkit_state_offline, Toast.LENGTH_LONG).show();
        } else if (state != DiscoveryListener.STATE_ON) {
            Toast.makeText(this, R.string.p2pkit_state_suspended, Toast.LENGTH_LONG).show();
        }
    }
  
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapActivity);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }
                        else {
                            Toast.makeText(mapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException,: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

        // Add a marker in Sydney, Australia, and move the camera.
//        LatLng sydney = new LatLng(27.527853, 68.758755);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void moveCamera(LatLng latlng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissions,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (P2PKit.isEnabled()) {
            P2PKit.setDiscoveryPowerMode(DiscoveryPowerMode.HIGH_PERFORMANCE);
        }
    }

    private void handlePeerDiscovered(final Peer peer) {
        UUID peerId = peer.getPeerId();
        byte[] peerDiscoveryInfo = peer.getDiscoveryInfo();
        float proximityStrength = (peer.getProximityStrength() - 1f) / 4;
        boolean proximityStrengthImmediate = peer.getProximityStrength() == ProximityStrength.IMMEDIATE;
    }

    private void handlePeerLost(final Peer peer) {
        UUID peerId = peer.getPeerId();
    }

    private void handlePeerUpdatedDiscoveryInfo(final Peer peer) {
        UUID peerId = peer.getPeerId();
        byte[] peerDiscoveryInfo = peer.getDiscoveryInfo();
    }

    private void handlePeerChangedProximityStrength(final Peer peer) {
        UUID peerId = peer.getPeerId();
        float proximityStrength = (peer.getProximityStrength() - 1f) / 4;
        boolean proximityStrengthImmediate = peer.getProximityStrength() == ProximityStrength.IMMEDIATE;
    }

    private void updateOwnDiscoveryInfo(int colorCode) {
        if (!P2PKit.isEnabled()) {
            Toast.makeText(this, R.string.p2pkit_not_enabled, Toast.LENGTH_LONG).show();
            return;
        }

        UUID ownNodeId = P2PKit.getMyPeerId();
    }
  
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }
}
