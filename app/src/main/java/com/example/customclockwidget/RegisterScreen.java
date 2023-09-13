package com.example.customclockwidget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterScreen extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);
        Button registerButton = (Button) findViewById(R.id.RegisterButton);
        EditText emailBox = (EditText) findViewById(R.id.RegisterEmail);
        EditText passwordBox = (EditText) findViewById(R.id.RegisterPassword);
        registerButton.setOnClickListener((view) -> {
            Log.d("Email:", emailBox.getText().toString());
            Log.d("Password:", passwordBox.getText().toString());
            mAuth.createUserWithEmailAndPassword(emailBox.getText().toString(), passwordBox.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d("UserCreate", "createUserWithEmail:success");
                            Intent intent = new Intent(RegisterScreen.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w("UserCreate", "createUserWithEmail:failure", task.getException());
                        }
                    });

        });
    }
}