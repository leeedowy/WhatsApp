package com.example.whatsapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {

    public Chat() {
    }

    public Chat(List members, Message message) {
        put("members", members);
        add("messages", message);
    }

    public void addMessage(Message message) {
        add("messages", message);
    }
}
