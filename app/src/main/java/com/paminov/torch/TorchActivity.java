package com.paminov.torch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class TorchActivity extends Activity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor lightSensor;
    Boolean isLightOn = false;
    Camera camera;

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            Log.e("Torch", String.format("Exception raised: %s", e));
        }
        return c;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor == lightSensor) {
            switch (accuracy) {
                case 0:
                    Toast.makeText(getApplicationContext(), "Unreliable", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "Low Accuracy", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "Medium Accuracy", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "High Accuracy", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(lightSensor==null) {
            Toast.makeText(getApplicationContext(), "This device doesn't have proximity sensor", Toast.LENGTH_SHORT).show();
        } else if (!checkCameraHardware(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "This device doesn't have a camera", Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        camera = getCameraInstance();
    }

    public void clickClose(View view) {
        finish();
        System.exit(0);
    }

    protected void onResume()   {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause()    {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void setLight(boolean status) {
        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = camManager.getCameraIdList()[0]; // Usually front camera is at 0 position and back camera is 1.
            camManager.setTorchMode(cameraId, status);
        } catch (Exception e) {
            Log.e("Torch", String.format("Exception while genabling torch: %s", e) );
        }
    }

    private void turnLightOn() {
        TextView on_off_status = findViewById(R.id.on_off_status);
        ImageView light = findViewById(R.id.light_img);
        setLight(true);
        isLightOn = true;
        on_off_status.setText("On");
        light.setImageResource(R.drawable.light_on);
    }

    private void turnLightOff() {
        TextView on_off_status = findViewById(R.id.on_off_status);
        ImageView light = findViewById(R.id.light_img);
        setLight(false);
        isLightOn = false;
        on_off_status.setText("Off");
        light.setImageResource(R.drawable.light_off);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView sensor_reading = findViewById(R.id.sensor_reading);
        sensor_reading.setText(String.format("%s lux",  event.values[0] ));

        if (event.values[0] < 25) {
            turnLightOn();
        } else {
            turnLightOff();
        }
    }


}
