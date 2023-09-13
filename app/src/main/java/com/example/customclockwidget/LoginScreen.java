package com.example.customclockwidget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        mAuth = FirebaseAuth.getInstance();
        Button loginButton = (Button) findViewById(R.id.LoginButton);
        EditText emailBox = (EditText) findViewById(R.id.LoginEmail);
        EditText passwordBox = (EditText) findViewById(R.id.LoginPassword);

        loginButton.setOnClickListener((view) -> {
            Log.d("Login Button", "Clicked");
            mAuth.signInWithEmailAndPassword(emailBox.getText().toString(), passwordBox.getText().toString())
                    .addOnCompleteListener(this, (task)->{
                        if (task.isSuccessful()) {
                            Log.d("Login", "Failed");
                            Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("Login", "Failed");
                        }
                    });
        });
        //https://stackoverflow.com/questions/10696986/how-to-set-the-part-of-the-text-view-is-clickable
        SpannableString registerButton = new SpannableString("Not Registered? Register Now");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(LoginScreen.this, RegisterScreen.class));
            }
        };
        registerButton.setSpan(clickableSpan, 16, registerButton.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView registerText = (TextView) findViewById(R.id.RegisterNowText);
        registerText.setText(registerButton);
        registerText.setMovementMethod(LinkMovementMethod.getInstance());


    }
}