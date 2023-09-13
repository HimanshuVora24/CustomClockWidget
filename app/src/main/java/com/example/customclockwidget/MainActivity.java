package com.example.customclockwidget;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    SeekBar redBar, greenBar, blueBar;
    int red, green, blue;
    SurfaceView colorView;
    SharedPreferences sp;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (mAuth.getCurrentUser() == null) {
            //https://stackoverflow.com/a/24205399
            Intent intent = new Intent(MainActivity.this, LoginScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("Settings", 0);
        red = sp.getInt("Red", 0);
        green = sp.getInt("Green", 0);
        blue = sp.getInt("Blue", 0);

        redBar = (SeekBar) findViewById(R.id.redSeekBar);
        greenBar = (SeekBar) findViewById(R.id.greenSeekBar);
        blueBar = (SeekBar) findViewById(R.id.blueSeekBar);
        redBar.setProgress(red);
        greenBar.setProgress(green);
        blueBar.setProgress(blue);
        redBar.setOnSeekBarChangeListener(this);
        greenBar.setOnSeekBarChangeListener(this);
        blueBar.setOnSeekBarChangeListener(this);

        colorView = (SurfaceView) findViewById(R.id.colorview);
        colorView.setBackgroundColor(Color.rgb(red, green, blue));

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener((view) -> {
            mAuth.signOut();
            Log.d("Logout", "Clicked");
            Intent intent = new Intent(MainActivity.this, LoginScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            AtomicBoolean permissionGranted = new AtomicBoolean(false);
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                    permissionGranted.set(true);
                                    retrieveWeather();
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                    permissionGranted.set(true);
                                    retrieveWeather();
                                } else {
                                    permissionGranted.set(false);
                                }
                            }
                    );


            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            retrieveWeather();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.saveButton) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("Red", red);
            editor.putInt("Green", green);
            editor.putInt("Blue", blue);
            editor.commit();
            Intent intent = new Intent(this, ClockWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(new ComponentName(getApplication(), ClockWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);
            //finishAndRemoveTask();
        }
    }

    /*@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }*/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(redBar)) red = progress;
        else if (seekBar.equals(blueBar)) blue = progress;
        else if (seekBar.equals(greenBar)) green = progress;

        colorView.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @SuppressLint("MissingPermission")
    public void retrieveWeather() {
        RequestQueue queue = Volley.newRequestQueue(this);;
        TextView weatherView = (TextView) findViewById(R.id.WeatherText);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        String url = "https://api.open-meteo.com/v1/forecast/?latitude="+location.getLatitude()
                                +"&longitude="+location.getLongitude()+"&current_weather=true";
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                response -> {
                                    // Display the first 500 characters of the response string.
                                    try {
                                        JSONObject weatherJson = new JSONObject(response);
                                        weatherView.setText("Weather: " + weatherJson.getJSONObject("current_weather").getString("temperature") + "C");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("Weather", "Error");
                                    }

                                },
                                error -> weatherView.setText("That didn't work!"));
                        Log.d("Latitude", url);
                        queue.add(stringRequest);
                    }
                });
    }
}