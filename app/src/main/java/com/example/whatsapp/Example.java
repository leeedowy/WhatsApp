package com.example.whatsapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Example")
public class Example extends ParseObject {

    public Example() {
    }

    public Example(int sample1, String sample2) {
        put("sample1", sample1);
        put("sample2", sample2);
    }

    public Example(Message message) {
        put("message", message);
    }
}
