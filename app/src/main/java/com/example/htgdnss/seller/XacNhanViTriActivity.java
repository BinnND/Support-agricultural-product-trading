package com.example.htgdnss.seller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.htgdnss.databinding.ActivityXacNhanViTriBinding;
import com.example.htgdnss.model.Product;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class XacNhanViTriActivity extends AppCompatActivity  {

    private static final int LOCATION_PERMISSION_CODE = 300;

    private ActivityXacNhanViTriBinding binding;
    private Product productDang;

    private double currentLat = 0, currentLng = 0;
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid"));
        try {
            Class.forName("org.osmdroid.config.Configuration").getMethod("setLogger", Class.forName("org.osmdroid.config.ILogger"))
                    .invoke(null, new Object() {
                        public void d(String tag, String message) {}
                        public void i(String tag, String message) {}
                        public void w(String tag, String message) {}
                        public void e(String tag, String message) {}
                    });
        } catch (Exception ignored) {}

        binding = ActivityXacNhanViTriBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding = ActivityXacNhanViTriBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Nhận dữ liệu sản phẩm từ Intent
        productDang = (Product) getIntent().getSerializableExtra("product");
        if (productDang == null) {
            productDang = new Product();
        }

        setupMap();
        fusedClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        binding.btnXacNhanViTri.setOnClickListener(v -> xacNhanViTri());
        xinQuyenViTri();
    }

    private void setupMap() {
        binding.map.setMultiTouchControls(true);
        binding.map.getController().setZoom(15.0);
    }

    private void xinQuyenViTri() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        binding.toolbar.setVisibility(View.VISIBLE);

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateLocation(location);
                    } else {
                        requestSingleUpdate();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.toolbar.setVisibility(View.GONE);
                    hienLoiGPS("Không thể lấy vị trí: " + e.getMessage());
                });
    }

    @SuppressLint("MissingPermission")
    private void requestSingleUpdate() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    updateLocation(location);
                } else {
                    binding.toolbar.setVisibility(View.GONE);
                    hienLoiGPS("Không thể xác định vị trí hiện tại");
                }
            }
        };

        fusedClient.requestLocationUpdates(request, locationCallback, getMainLooper());
    }

    private void updateLocation(Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();

        binding.toolbar.setVisibility(View.GONE);
        binding.cardViTri.setVisibility(View.VISIBLE);

        GeoPoint point = new GeoPoint(currentLat, currentLng);
        binding.map.getController().setZoom(16.0);
        binding.map.getController().setCenter(point);

        binding.map.getOverlays().clear();
        Marker marker = new Marker(binding.map);
        marker.setPosition(point);
        marker.setTitle("Vị trí của bạn");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        binding.map.getOverlays().add(marker);
        binding.map.invalidate();

        layDiaChi(currentLat, currentLng);
    }

    private void layDiaChi(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
                List<Address> list = geocoder.getFromLocation(lat, lng, 1);

                if (list != null && !list.isEmpty()) {
                    Address addr = list.get(0);
                    String diaChi = addr.getAddressLine(0);
                    productDang.setLocation(diaChi);

                    runOnUiThread(() -> {
                        binding.tvViTri.setText(diaChi);
                        binding.tvViTri.setVisibility(View.VISIBLE);
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() -> binding.tvViTri.setText(lat + ", " + lng));
            }
        }).start();
    }

    private void xacNhanViTri() {
        if (currentLat == 0 && currentLng == 0) {
            hienLoiGPS("Chưa lấy được vị trí GPS! Vui lòng thử lại.");
            return;
        }

        productDang.setLatitude(currentLat);
        productDang.setLongitude(currentLng);

        Intent intent = new Intent(this, XacNhanDangActivity.class);
        intent.putExtra("product", productDang);
        startActivity(intent);
        finish();
    }

    private void hienLoiGPS(String msg) {
        binding.tvGpsError.setText(msg);
        binding.tvGpsError.setVisibility(View.VISIBLE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                hienLoiGPS("Bạn cần cấp quyền truy cập vị trí để sử dụng tính năng này!");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null && fusedClient != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
        if (binding.map != null) {
            binding.map.onDetach();
        }
    }
}