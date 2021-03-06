package com.example.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, Runnable{

    private ArrayList<String> usernames;
    private ArrayAdapter<String> adapter;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        usernames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usernames);
        ListView usersListView = findViewById(R.id.usersListView);
        usersListView.setOnItemClickListener(this);
        usersListView.setAdapter(adapter);

        // Testing ---------------------------------------------
        usersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ParseQuery<Chat> chatQuery = ParseQuery.getQuery(Chat.class);
                chatQuery.whereContainsAll("members", Arrays.asList(ParseUser.getCurrentUser().getUsername(), usernames.get(position)));
                chatQuery.findInBackground(new FindCallback<Chat>() {
                    @Override
                    public void done(final List<Chat> chats, ParseException e) {
                        if (e == null) {
                            if (chats.size() == 1) {
                                if (chats.get(0).getList("messages").size() > 0) {
                                    List<Message> messages = chats.get(0).getList("messages");
                                    ParseObject.deleteAllInBackground(messages, new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                List<Message> list = Collections.emptyList();
                                                chats.get(0).put("messages", list);
                                                chats.get(0).saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            Toast.makeText(getApplicationContext(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

                return true;
            }
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.users_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOutItem:
                ParseUser.logOut();
                MainActivity.goToActivity(this, MainActivity.class);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        Intent intent = new Intent(UsersActivity.this, ChatActivity.class);
        intent.putExtra("recipient", usernames.get(position));
        startActivity(intent);
    }

    @Override
    public void run() {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        userQuery.whereEqualTo("app", "whatsapp");

        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    usernames.clear();
                    for (ParseUser pu : objects) {
                        usernames.add(pu.getUsername());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    new ParseDebugger().sendExceptionData(e);
                }
            }
        });

        handler.postDelayed(this, 5000);
    }
}
