package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.backgroundConLayout).setOnClickListener(this);
        findViewById(R.id.logoImageView).setOnClickListener(this);
        findViewById(R.id.appNameTextView).setOnClickListener(this);
    }

    public void attemptToSignUp(View view) {
        Log.i("Result", "hello");
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageView || v instanceof ConstraintLayout || v instanceof TextView) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
