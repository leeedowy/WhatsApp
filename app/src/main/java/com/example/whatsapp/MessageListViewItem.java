package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MessageListViewItem {

    private String content;
    private int index;
    private String key;

    public MessageListViewItem(String content, int index, String key) {
        this.content = content;
        this.index = index;
        this.key = key;
    }

    @NonNull
    @Override
    public String toString() {
        return content;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return content.equals(obj);
    }

    public String getContent() {
        return content;
    }

    public int getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }
}
