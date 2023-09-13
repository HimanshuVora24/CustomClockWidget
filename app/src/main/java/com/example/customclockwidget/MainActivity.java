package com.example.customclockwidget;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    SeekBar redBar, greenBar, blueBar;
    int red, green, blue;
    SurfaceView colorView;
    SharedPreferences sp;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
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
}