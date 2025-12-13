package com.sunit.groceryplus;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class OrderTrackingActivity extends AppCompatActivity {

    private MapView map;
    private int orderId;
    private String orderStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSMDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        setContentView(R.layout.activity_order_tracking);

        // Get intent data
        orderId = getIntent().getIntExtra("order_id", -1);
        orderStatus = getIntent().getStringExtra("order_status");

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Track Order #" + orderId);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        TextView statusTv = findViewById(R.id.orderStatusTv);
        if (orderStatus != null) {
            statusTv.setText("Status: " + orderStatus);
        }

        // Setup Map
        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        
        // Simulating locations: Warehouse -> User Location
        // Using generic coordinates (e.g., Kathmandu, Nepal for demo)
        GeoPoint startPoint = new GeoPoint(27.7172, 85.3240); // Warehouse
        GeoPoint endPoint = new GeoPoint(27.7000, 85.3000);   // User

        map.getController().setCenter(startPoint);

        // Add Markers
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Warehouse");
        map.getOverlays().add(startMarker);

        Marker endMarker = new Marker(map);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setTitle("Delivery Location");
        map.getOverlays().add(endMarker);

        // Draw Path (Straight line for demo)
        List<GeoPoint> geoPoints = new ArrayList<>();
        geoPoints.add(startPoint);
        geoPoints.add(endPoint);

        Polyline line = new Polyline();
        line.setPoints(geoPoints);
        line.setColor(0xFF0000FF); // Blue
        map.getOverlays().add(line);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
