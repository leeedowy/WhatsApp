package com.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements Runnable, View.OnTouchListener {

    public static final int EMPTY = 0;
    public static final int POPULATED = 1;
    public static final int NULL = 2;
    private EditText messageEditText;
    private String[] viewKeys;
    private List<Map<String, MessageListViewItem>> messages;
    private SimpleAdapter adapter;
    private ListView chatListView;
    private String recipient;
    private TextView emptyChatTextView;
    private Handler handler;
    private Chat currentChat;
    private int chatState = NULL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.messageEditText);
        final ImageButton sendButton = findViewById(R.id.sendButton);

        sendButton.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) sendButton.getLayoutParams();
                params.height = messageEditText.getHeight();
                sendButton.setLayoutParams(params);
            }
        });

        viewKeys = new String[] {"content", "author"};
        int[] rowViews = new int [] {R.id.messageTextView, R.id.authorTextView};

        messages = new ArrayList<>();

        adapter = new SimpleAdapter(this, messages, R.layout.message_list, viewKeys, rowViews);
        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object object, String value) {
                TextView textView = (TextView) view;
                MessageListViewItem item = (MessageListViewItem) object;
                LinearLayout linearLayout = (LinearLayout) view.getParent();

                textView.setText(value);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                float multiplier = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
                int sideMargins = (int) (multiplier * 10);
                int topAndBotMargins = (int) (multiplier * 7);
                params.setMargins(sideMargins, topAndBotMargins, sideMargins, topAndBotMargins);

                int gravity;
                if (messages.get(item.getIndex()).get("author").equals(ParseUser.getCurrentUser().getUsername())) {
                    gravity = Gravity.END;
                } else {
                    gravity = Gravity.START;
                }
                textView.setGravity(gravity);
                linearLayout.setGravity(gravity);
                params.gravity = gravity;
                linearLayout.setLayoutParams(params);

                return true;
            }
        };
        adapter.setViewBinder(binder);

        chatListView = findViewById(R.id.chatListView);
        chatListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        emptyChatTextView = findViewById(R.id.emptyChatTextView);

        messageEditText.setOnTouchListener(this);

        Intent intent = getIntent();
        recipient = intent.getStringExtra("recipient");

        handler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(this, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(this);
    }

    public void updateChat() {
        ParseQuery<Chat> chatQuery = ParseQuery.getQuery(Chat.class);
        chatQuery.whereContainsAll("members", Arrays.asList(ParseUser.getCurrentUser().getUsername(), recipient));
        chatQuery.findInBackground(new FindCallback<Chat>() {
            @Override
            public void done(List<Chat> chats, ParseException e) {
                if (e == null) {
                    if (chats.size() == 1) {
                        currentChat = chats.get(0);

                        if (chats.get(0).getList("messages").size() > 0) {
                            chatState = POPULATED;
                            emptyChatTextView.setVisibility(View.INVISIBLE);

                            Message m;
                            ArrayList<String> messageIds = new ArrayList<>();
                            for (Object o : chats.get(0).getList("messages")) {
                                m = (Message) o;
                                messageIds.add(m.getObjectId());
                            }

                            ParseQuery<Message> messageQuery = ParseQuery.getQuery(Message.class);
                            messageQuery.whereContainedIn("objectId", messageIds);
                            messageQuery.addAscendingOrder("createdAt");
                            messageQuery.findInBackground(new FindCallback<Message>() {
                                @Override
                                public void done(List<Message> messagesParse, ParseException e) {
                                    if (e == null) {
                                        HashMap<String, MessageListViewItem> map;
                                        messages.clear();
                                        for (Message m : messagesParse) {
                                            map = new HashMap<>();
                                            map.put(viewKeys[0], new MessageListViewItem(m.get(viewKeys[0]), messages.size()));
                                            map.put(viewKeys[1], new MessageListViewItem(m.get(viewKeys[1]), messages.size()));
                                            messages.add(map);
                                        }

                                        adapter.notifyDataSetChanged();
                                    } else {
                                        new ParseDebugger().sendExceptionData(e);
                                        Toast.makeText(getApplicationContext(), R.string.get_chat_fail_toast, Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                        finish();
                                    }
                                }
                            });
                        } else {
                            chatState = EMPTY;
                            messages.clear();
                            adapter.notifyDataSetChanged();
                            emptyChatTextView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        chatState = NULL;
                        messages.clear();
                        adapter.notifyDataSetChanged();
                        emptyChatTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    new ParseDebugger().sendExceptionData(e);
                    Toast.makeText(getApplicationContext(), R.string.get_chat_fail_toast, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    finish();
                }
            }
        });
    }

    /* OnClicks */

    public void sendMessage(View view) {
        if (!messageEditText.getText().toString().equals("")) {
            handler.removeCallbacks(this);

            HashMap<String, MessageListViewItem> map = new HashMap<>();
            map.put(viewKeys[0], new MessageListViewItem(messageEditText.getText().toString(), messages.size()));
            map.put(viewKeys[1], new MessageListViewItem(ParseUser.getCurrentUser().getUsername(), messages.size()));
            messages.add(map);

            adapter.notifyDataSetChanged();
            chatListView.smoothScrollToPosition(messages.size() - 1);

            final String messageBuffer = messageEditText.getText().toString();
            messageEditText.setText("");

            if (chatState != NULL) {
                currentChat.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            currentChat.addMessage(new Message(messageBuffer, ParseUser.getCurrentUser().getUsername()));
                            currentChat.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        new ParseDebugger().sendExceptionData(e);
                                        Toast.makeText(ChatActivity.this, R.string.send_message_fail_toast, Toast.LENGTH_LONG).show();
                                        messageEditText.setText(messageBuffer);
                                        e.printStackTrace();
                                    }

                                    handler.postDelayed(ChatActivity.this, 0);
                                }
                            });
                        } else {
                            new ParseDebugger().sendExceptionData(e);
                            Toast.makeText(ChatActivity.this, R.string.send_message_fail_toast, Toast.LENGTH_LONG).show();
                            messageEditText.setText(messageBuffer);
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Chat chat = new Chat(Arrays.asList(ParseUser.getCurrentUser().getUsername(), recipient), new Message(messageBuffer, ParseUser.getCurrentUser().getUsername()));
                chat.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            new ParseDebugger().sendExceptionData(e);
                            Toast.makeText(ChatActivity.this, R.string.send_message_fail_toast, Toast.LENGTH_LONG).show();
                            messageEditText.setText(messageBuffer);
                            e.printStackTrace();
                        }

                        handler.postDelayed(ChatActivity.this, 0);
                    }
                });
            }
        }
    }

    /* /OnClicks */

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                chatListView.smoothScrollToPosition(messages.size() - 1);
            }
        }, 200);

        return false;
    }

    @Override
    public void run() {
        updateChat();
        handler.postDelayed(this, 1000);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
