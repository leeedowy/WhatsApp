package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.backgroundConLayout).setOnClickListener(this);
        findViewById(R.id.logoImageView).setOnClickListener(this);
        findViewById(R.id.appNameTextView).setOnClickListener(this);
    }

    public static void goToActivity(Activity thisActivity, Class<?> destination) {
        Intent intent = new Intent(thisActivity, destination);
        thisActivity.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ParseUser.getCurrentUser() != null) {
            goToActivity(this, UsersActivity.class);
            finish();
        }
    }

    public void attemptToLogIn(View view) {
        final EditText userEdtTxt = findViewById(R.id.logInUserEditText);
        final EditText passEdtTxt = findViewById(R.id.logInPassEditText);

        ParseUser.logInInBackground(userEdtTxt.getText().toString(), passEdtTxt.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    Toast.makeText(getApplicationContext(), R.string.log_in_succ_toast, Toast.LENGTH_LONG).show();
                    goToActivity(MainActivity.this, UsersActivity.class);

                    userEdtTxt.setText("");
                    passEdtTxt.setText("");

                    finish();
                } else {
                    Toast.makeText(MainActivity.this, R.string.log_in_fail_toast, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void goToSignUpActivity(View view) {
        goToActivity(this, SignUpActivity.class);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageView || v instanceof ConstraintLayout || v instanceof TextView && v.getId() != R.id.signInTextView) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
