package com.example.vtop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Splash screen duration in milliseconds
    private static final int SPLASH_DURATION = 3000;

    private ImageView imageBox;
    private TextView textBox, textBox0, welcomeTextView, textVTOP;
    private Animation fadeInAnimation;
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imageBox = findViewById(R.id.imageBox0);
        textBox = findViewById(R.id.textBox);
        textBox0 = findViewById(R.id.textBox0);
        layout = findViewById(R.id.layout0);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        textVTOP = findViewById(R.id.textVTOP);

        // Load the fade-in animation from XML
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);

        // Start the animation for the ImageView and TextView
        imageBox.startAnimation(fadeInAnimation);
        textBox.startAnimation(fadeInAnimation);
        textBox0.startAnimation(fadeInAnimation);
        layout.startAnimation(fadeInAnimation);
        welcomeTextView.setAnimation(fadeInAnimation);
        textVTOP.setAnimation(fadeInAnimation);

        // Delayed start of the StartupActivity after the splash screen duration
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the StartupActivity
                Intent intent = new Intent(SplashActivity.this, StartupActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
