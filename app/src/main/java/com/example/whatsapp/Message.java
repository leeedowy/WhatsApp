package com.example.whatsapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Message")
public class Message extends ParseObject {

    public Message() {
    }

    public Message(String content, String author) {
        put("content", content);
        put("author", author);
    }
}
