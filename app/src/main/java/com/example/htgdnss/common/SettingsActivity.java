package com.example.htgdnss.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.htgdnss.R;
import com.example.htgdnss.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity implements SensorEventListener {

    private ActivitySettingsBinding binding;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor accelerometerSensor;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LIGHT_SENSOR = "light_sensor_enabled";
    private static final String KEY_ACCELEROMETER = "accelerometer_enabled";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private boolean isLightSensorEnabled = false;
    private boolean isAccelerometerEnabled = false;
    private boolean isDarkModeFromSensor = false;

    private Handler handler = new Handler();
    private long lastShakeTime = 0;
    private static final long SHAKE_COOLDOWN = 1000; // 1 giây cooldown

    private int originalBackgroundColor = Color.WHITE; // Lưu màu gốc

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Lưu màu background gốc
        originalBackgroundColor = Color.WHITE;
        binding.getRoot().setBackgroundColor(originalBackgroundColor);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Lấy sensors
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Kiểm tra sensors có tồn tại không
        if (lightSensor == null) {
            binding.switchLight.setEnabled(false);
            binding.tvLightValue.setText("Thiết bị không hỗ trợ cảm biến ánh sáng");
        }
        if (accelerometerSensor == null) {
            binding.switchAccelerometer.setEnabled(false);
            binding.tvAccelerometerValue.setText("Thiết bị không hỗ trợ cảm biến gia tốc");
        }

        // Đọc cài đặt đã lưu
        loadSettings();

        // Xử lý sự kiện switch
        binding.switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLightSensorEnabled = isChecked;
            saveSettings();
            if (isChecked && lightSensor != null) {
                sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(this, "Đã bật cảm biến ánh sáng", Toast.LENGTH_SHORT).show();
            } else {
                sensorManager.unregisterListener(this, lightSensor);
                binding.tvLightValue.setText("Giá trị: -- lux");
                Toast.makeText(this, "Đã tắt cảm biến ánh sáng", Toast.LENGTH_SHORT).show();
            }
        });

        binding.switchAccelerometer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAccelerometerEnabled = isChecked;
            saveSettings();
            if (isChecked && accelerometerSensor != null) {
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(this, "Đã bật cảm biến gia tốc (lắc để đổi màu)", Toast.LENGTH_SHORT).show();
            } else {
                sensorManager.unregisterListener(this, accelerometerSensor);
                binding.tvAccelerometerValue.setText("X: 0.0 | Y: 0.0 | Z: 0.0");

                // KHI TẮT SENSOR, TRẢ VỀ MÀU BAN ĐẦU
                changeBackgroundColorSmooth(originalBackgroundColor);
                Toast.makeText(this, "Đã tắt cảm biến gia tốc, trở về màu nền mặc định", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị thông tin thiết bị
        displayDeviceInfo();
    }

    private void loadSettings() {
        isLightSensorEnabled = sharedPreferences.getBoolean(KEY_LIGHT_SENSOR, false);
        isAccelerometerEnabled = sharedPreferences.getBoolean(KEY_ACCELEROMETER, false);
        isDarkModeFromSensor = sharedPreferences.getBoolean(KEY_DARK_MODE, false);

        binding.switchLight.setChecked(isLightSensorEnabled);
        binding.switchAccelerometer.setChecked(isAccelerometerEnabled);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LIGHT_SENSOR, isLightSensorEnabled);
        editor.putBoolean(KEY_ACCELEROMETER, isAccelerometerEnabled);
        editor.putBoolean(KEY_DARK_MODE, isDarkModeFromSensor);
        editor.apply();
    }

    private void displayDeviceInfo() {
        String deviceInfo = "Model: " + Build.MODEL + "\n" +
                "Manufacturer: " + Build.MANUFACTURER + "\n" +
                "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n" +
                "Light Sensor: " + (lightSensor != null ? "Có" : "Không") + "\n" +
                "Accelerometer: " + (accelerometerSensor != null ? "Có" : "Không");
        binding.tvDeviceInfo.setText(deviceInfo);
    }

    // PHƯƠNG THỨC CHUYỂN MÀU NỀN MƯỢT MÀ TRONG 0.5s
    private void changeBackgroundColorSmooth(int newColor) {
        View rootView = binding.getRoot();
        int currentColor = originalBackgroundColor;

        try {
            if (rootView.getBackground() != null) {
                if (rootView.getBackground() instanceof android.graphics.drawable.ColorDrawable) {
                    currentColor = ((android.graphics.drawable.ColorDrawable) rootView.getBackground()).getColor();
                }
            }
        } catch (Exception e) {
            currentColor = originalBackgroundColor;
        }

        // Chuyển màu từ từ trong 500ms (0.5 giây)
        animateColorChange(rootView, currentColor, newColor, 500);
    }

    // HIỆU ỨNG CHUYỂN MÀU ANIMATION
    private void animateColorChange(final View view, final int fromColor, final int toColor, final long durationMs) {
        final long startTime = System.currentTimeMillis();
        final Handler handler = new Handler();

        final float fromA = Color.alpha(fromColor) / 255f;
        final float fromR = Color.red(fromColor) / 255f;
        final float fromG = Color.green(fromColor) / 255f;
        final float fromB = Color.blue(fromColor) / 255f;

        final float toA = Color.alpha(toColor) / 255f;
        final float toR = Color.red(toColor) / 255f;
        final float toG = Color.green(toColor) / 255f;
        final float toB = Color.blue(toColor) / 255f;

        final Runnable animator = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float fraction = Math.min(1.0f, (float) elapsed / durationMs);

                float a = fromA + (toA - fromA) * fraction;
                float r = fromR + (toR - fromR) * fraction;
                float g = fromG + (toG - fromG) * fraction;
                float b = fromB + (toB - fromB) * fraction;

                int color = Color.argb((int)(a * 255), (int)(r * 255), (int)(g * 255), (int)(b * 255));
                view.setBackgroundColor(color);

                if (fraction < 1.0f) {
                    handler.postDelayed(this, 16); // ~60fps
                }
            }
        };

        handler.post(animator);
    }

    // LẤY MÀU NGẪU NHIÊN ĐẸP
    private int getRandomColor() {
        int[][] colors = {
                {33, 150, 243},   // Xanh dương
                {76, 175, 80},    // Xanh lá
                {255, 87, 34},    // Cam
                {156, 39, 176},   // Tím
                {0, 150, 136},    // Xanh ngọc
                {255, 193, 7},    // Vàng
                {233, 30, 99},    // Hồng
                {63, 81, 181},    // Xanh đậm
                {255, 152, 0},    // Cam đậm
                {139, 195, 74},   // Xanh lá nhạt
                {255, 255, 255},  // Trắng
                {158, 158, 158}   // Xám (Material Gray 500)
        };
        int[] randomColor = colors[(int) (Math.random() * colors.length)];
        return Color.rgb(randomColor[0], randomColor[1], randomColor[2]);
    }

    // SensorEventListener methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT && isLightSensorEnabled) {
            float lightValue = event.values[0];
            binding.tvLightValue.setText(String.format("Giá trị: %.1f lux", lightValue));

            // Tự động chuyển Dark Mode khi ánh sáng yếu (< 10 lux)
            if (lightValue < 10) {
                if (!isDarkModeFromSensor) {
                    isDarkModeFromSensor = true;
                    saveSettings();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(this, "Đã chuyển sang Dark Mode (ánh sáng yếu: " + (int)lightValue + " lux)", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isDarkModeFromSensor) {
                    isDarkModeFromSensor = false;
                    saveSettings();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(this, "Đã chuyển sang Light Mode (ánh sáng mạnh: " + (int)lightValue + " lux)", Toast.LENGTH_SHORT).show();
                }
            }
        }

        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isAccelerometerEnabled) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            binding.tvAccelerometerValue.setText(String.format("X: %.2f | Y: %.2f | Z: %.2f", x, y, z));

            // Phát hiện lắc điện thoại (khi gia tốc > 15)
            double acceleration = Math.sqrt(x * x + y * y + z * z);

            // Kiểm tra cooldown để tránh đổi màu quá nhiều lần
            long currentTime = System.currentTimeMillis();
            if (acceleration > 10 && (currentTime - lastShakeTime) > SHAKE_COOLDOWN) {
                lastShakeTime = currentTime;

                // Đổi màu nền mượt mà
                int newColor = getRandomColor();
                changeBackgroundColorSmooth(newColor);

                // Hiển thị toast thông báo
                Toast.makeText(this, "Đã đổi màu nền! Lắc tiếp để đổi màu khác.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký sensors
        if (isLightSensorEnabled && lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (isAccelerometerEnabled && accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký sensors để tiết kiệm pin
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}