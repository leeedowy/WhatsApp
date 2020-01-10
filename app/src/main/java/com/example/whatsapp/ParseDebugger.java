package com.example.whatsapp;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.PrintWriter;
import java.io.StringWriter;

@ParseClassName("Debugger")
public class ParseDebugger extends ParseObject {

    public ParseDebugger() {
    }

    public void sendExceptionData(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        put("username", "not logged in");
        put("message", "message not found");
        put("cause", "cause not found");

        if (ParseUser.getCurrentUser() != null) {
            put("username", ParseUser.getCurrentUser().getUsername());
        }

        if (e.getMessage() != null) {
            put("message", e.getMessage());
        }

        if (e.getCause() != null) {
            put("cause", e.getCause().toString());
        }

        put("stackTrace", sw.toString());
        saveInBackground();
    }
}
