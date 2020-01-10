package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MessageListViewItem {

    private String content;
    private int index;

    public MessageListViewItem(Object content, int index) {
        this.content = (String) content;
        this.index = index;
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

    public int getIndex() {
        return index;
    }
}
