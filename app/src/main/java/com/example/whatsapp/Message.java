package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@ParseClassName("Message")
public class Message extends ParseObject {

    public Message() {
    }

    public Message(String content, String author) {
        put("content", content);
        put("sentAt", new Date(System.currentTimeMillis()));
        put("author", author);
    }

    @NonNull
    @Override
    public String toString() {
        if (getString("content") == null || getDate("sentAt") == null || getString("author") == null) {
            return "null";
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss|SSS z", Locale.US);
        return getString("author") + ": " + getString("content") + " (" + dateFormat.format(getDate("sentAt")) + ")";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (getString("content") == null || getDate("sentAt") == null || getString("author") == null) {
            return false;
        }

        return obj instanceof Message &&
        getString("content").equals(((Message) obj).getString("content")) &&
        getDate("sentAt").equals(((Message) obj).getDate("sentAt")) &&
        getString("author").equals(((Message) obj).getString("author"));
    }
}
