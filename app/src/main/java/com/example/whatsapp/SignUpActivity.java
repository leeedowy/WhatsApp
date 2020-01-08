package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

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
        EditText userEdtTxt = findViewById(R.id.signUpUserEditText);
        EditText passEdtTxt = findViewById(R.id.signUpPassEditText);

        ParseUser userToSignUp = new ParseUser();
        userToSignUp.setUsername(userEdtTxt.getText().toString());
        userToSignUp.setPassword(passEdtTxt.getText().toString());
        userToSignUp.put("app", "whatsapp");

        userToSignUp.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), R.string.sign_up_succ_toast, Toast.LENGTH_LONG).show();
                    MainActivity.goToActivity(SignUpActivity.this, UsersActivity.class);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, R.string.sign_up_fail_toast, Toast.LENGTH_LONG).show();
                }
            }
        });
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

    @Override
    public void onBackPressed() {
        MainActivity.goToActivity(this, MainActivity.class);
        finish();
        super.onBackPressed();
    }
}
