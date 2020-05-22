package com.covtracers.covtrace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapActivity);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(27.527853, 68.758755);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
}
