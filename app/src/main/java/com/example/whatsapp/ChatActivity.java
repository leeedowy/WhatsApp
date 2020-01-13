package com.example.whatsapp;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements Runnable, View.OnTouchListener {

    public static final int EMPTY = 0;
    public static final int POPULATED = 1;
    public static final int NULL = 2;

    private int chatState = NULL;
    private boolean update = true;

    private EditText messageEditText;
    private String[] viewKeys = new String[] {"content", "author", "sentAt"};
    private List<Map<String, MessageListViewItem>> messages;
    private SimpleAdapter adapter;
    private ListView chatListView;
    private String recipient;
    private TextView emptyChatTextView;
    private Handler handler = new Handler();
    private Chat currentChat;

    private ArrayList<Message> justSentMessages = new ArrayList<>();

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

        int[] rowViews = new int [] {R.id.messageTextView, R.id.authorAndSentAtTextView, R.id.authorAndSentAtTextView};

        messages = new ArrayList<>();

        adapter = new SimpleAdapter(this, messages, R.layout.message_list, viewKeys, rowViews);
        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object object, String value) {
                TextView textView = (TextView) view;
                MessageListViewItem item = (MessageListViewItem) object;
                LinearLayout linearLayout = (LinearLayout) view.getParent();

                int gravity = Gravity.START;
                if (messages.get(item.getIndex()).get("author").equals(ParseUser.getCurrentUser().getUsername())) {
                    gravity = Gravity.END;
                }

                switch (item.getKey()) {
                    case "content":
                        textView.setText(value);

                        int authorStrSize = messages.get(item.getIndex()).get("author").toString().length();
                        int sentAtStrSize = toFormattedDate(messages.get(item.getIndex()).get("sentAt").toString()).length();
                        if (authorStrSize + sentAtStrSize + 2 >= value.length()) {
                            linearLayout.setGravity(gravity);
                        }
                        break;
                    case "author":
                        if (value.equals(ParseUser.getCurrentUser().getUsername())) {
                            textView.setText(R.string.author_you);
                        } else {
                            textView.setText(value);
                        }
                        break;
                    case "sentAt":
                        textView.setText(textView.getText() + ", " + toFormattedDate(value));
                }

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                float multiplier = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
                int sideMargins = (int) (multiplier * 10);
                int topAndBotMargins = (int) (multiplier * 7);
                params.setMargins(sideMargins, topAndBotMargins, sideMargins, topAndBotMargins);
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

    /* OnClicks */

    public void sendMessage(View view) {
        if (!messageEditText.getText().toString().equals("")) {
            update = false;
            final String messageBuffer = messageEditText.getText().toString();

            final Message message = new Message(messageBuffer, ParseUser.getCurrentUser().getUsername());
            justSentMessages.add(message);

            HashMap<String, MessageListViewItem> map = new HashMap<>();
            map.put(viewKeys[0], new MessageListViewItem(messageEditText.getText().toString(), messages.size(), viewKeys[0]));
            map.put(viewKeys[1], new MessageListViewItem(ParseUser.getCurrentUser().getUsername(), messages.size(), viewKeys[1]));
            map.put(viewKeys[2], new MessageListViewItem(String.valueOf(message.getDate(viewKeys[2]).getTime()), messages.size(), viewKeys[2]));
            messages.add(map);

            adapter.notifyDataSetChanged();
            chatListView.smoothScrollToPosition(messages.size() - 1);
            emptyChatTextView.setVisibility(View.INVISIBLE);

            messageEditText.setText("");

            if (chatState != NULL) {
                currentChat.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            currentChat.addMessage(message);
                            currentChat.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        new ParseDebugger().sendExceptionData(e);
                                        Toast.makeText(ChatActivity.this, R.string.send_message_fail_toast, Toast.LENGTH_LONG).show();
                                        messageEditText.setText(messageBuffer);
                                        e.printStackTrace();
                                    }
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
                    }
                });
            }
        }
    }

    /* /OnClicks */

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.messageEditText && chatListView.getLastVisiblePosition() == messages.size() - 1) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    chatListView.smoothScrollToPosition(messages.size() - 1);
                }
            }, 200);
        }

        return false;
    }

    @Override
    public void run() {
        handler.postDelayed(this, 2000);

        ParseQuery<Chat> chatQuery = ParseQuery.getQuery(Chat.class);
        chatQuery.whereContainsAll("members", Arrays.asList(ParseUser.getCurrentUser().getUsername(), recipient));
        chatQuery.include("messages.content");
        chatQuery.include("messages.sentAt");
        chatQuery.include("messages.author");
        chatQuery.findInBackground(new FindCallback<Chat>() {
            @Override
            public void done(List<Chat> chats, ParseException e) {
                if (e == null) {
                    if (chats.size() == 1) {
                        currentChat = chats.get(0);

                        if (chats.get(0).getList("messages").size() > 0) {
                            chatState = POPULATED;
                            emptyChatTextView.setVisibility(View.INVISIBLE);

                            List<Message> messagesParse = chats.get(0).getList("messages");
                            if (update) {
                                boolean lastBeforeReceive = (chatListView.getLastVisiblePosition() == chatListView.getCount() - 1);
                                int countBefore = messages.size();

                                HashMap<String, MessageListViewItem> map;
                                messages.clear();
                                for (Message m : messagesParse) {
                                    map = new HashMap<>();
                                    map.put(viewKeys[0], new MessageListViewItem(m.getString(viewKeys[0]), messages.size(), viewKeys[0]));
                                    map.put(viewKeys[1], new MessageListViewItem(m.getString(viewKeys[1]), messages.size(), viewKeys[1]));
                                    map.put(viewKeys[2], new MessageListViewItem(String.valueOf(m.getDate(viewKeys[2]).getTime()), messages.size(), viewKeys[2]));
                                    messages.add(map);
                                }

                                boolean lastAfterReceive = (chatListView.getLastVisiblePosition() == chatListView.getCount() - 2);
                                int countAfter = messages.size();

                                adapter.notifyDataSetChanged();
                                if (lastBeforeReceive && (!lastAfterReceive) && countBefore != countAfter) {
                                    chatListView.smoothScrollToPosition(chatListView.getCount() - 1);
                                }
                            } else if (justSentMessages.size() > 0) {
                                ArrayList<Message> toRemove = new ArrayList<>();
                                for (Message jsm : justSentMessages) {
                                    for (Message m : messagesParse) {
                                        if (jsm.equals(m)) {
                                            toRemove.add(jsm);
                                        }
                                    }
                                }

                                justSentMessages.removeAll(toRemove);
                                if (justSentMessages.size() == 0) {
                                    update = true;
                                }
                            }
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

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private String toFormattedDate(String epoch) {
        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(Long.parseLong(epoch));

        Calendar now = Calendar.getInstance();

        DateFormat hoursMinutes = new SimpleDateFormat("H:mm");

        Calendar clone = (Calendar) now.clone();
        clone.set(Calendar.MILLISECOND, 0);
        clone.set(Calendar.SECOND, 0);
        clone.set(Calendar.MINUTE, 0);
        clone.set(Calendar.HOUR_OF_DAY, 0);
        if (messageTime.compareTo(clone) >= 0) {
            return getString(R.string.today) + " " + hoursMinutes.format(messageTime.getTime());
        }

        clone.add(Calendar.DATE, -1);
        if (messageTime.compareTo(clone) >= 0) {
            return getString(R.string.yesterday) + " " + hoursMinutes.format(messageTime.getTime());
        }

        clone.add(Calendar.DATE, -5);
        if (messageTime.compareTo(clone) >= 0) {
            return getResources().getStringArray(R.array.week_days)[messageTime.get(Calendar.DAY_OF_WEEK) - 1];
        }

        clone.add(Calendar.DATE, -358);
        if (messageTime.compareTo(clone) >= 0) {
            return getResources().getStringArray(R.array.months)[messageTime.get(Calendar.MONTH)] + " " + messageTime.get(Calendar.DAY_OF_MONTH);
        }

        clone.add(Calendar.YEAR, -2);
        if (messageTime.compareTo(clone) >= 0) {
            return getResources().getStringArray(R.array.months)[messageTime.get(Calendar.MONTH)] + " " + messageTime.get(Calendar.YEAR);
        }

        return String.valueOf(messageTime.get(Calendar.YEAR));
    }
}
