package com.example.partymaker;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.partymaker.data.ChatAdpter;
import com.example.partymaker.data.ChatMessage;
import com.example.partymaker.data.DBref;

import java.util.ArrayList;
import java.util.HashMap;

public class Chat extends AppCompatActivity {

    private ListView lv4;
    private EditText etMessage;
    private ImageButton btnSend;
    private String GroupKey;
    private HashMap<String, Object> MessageKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //this 2 lines changes title's name
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Chat");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0081d1")));

        //connection between values from intent
        HashMap<String, Object> hashMessageKeys = (HashMap<String, Object>) getIntent().getSerializableExtra("MessageKeys");
        MessageKeys = hashMessageKeys;
        String strGroupKey = getIntent().getExtras().getString("groupKey", "defaultKey");
        GroupKey = strGroupKey;

        //connection
        lv4 = (ListView) findViewById(R.id.lv4);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);

        ShowData();
        eventHandler();
    }

    private void eventHandler() {
        lv4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        lv4.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                ChatMessage msg = new ChatMessage();

                //equals
                String Text = etMessage.getText().toString();
                String User = DBref.Auth.getCurrentUser().getEmail();

                //Date when writed message (Current time)
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());

                //create unique key for message
                String MessageKey = DBref.refMessages.push().getKey();

                //set
                msg.setMessageUser(User);
                msg.setMessageTime(strDate);
                msg.setMessageText(Text);
                msg.setMessageKey(MessageKey);

                //add message to Database
                DBref.refMessages.child(MessageKey).setValue(msg);

                // adding reference to messageKeys with the message key
                MessageKeys.put(MessageKey, "true");
                DBref.refGroups.child(GroupKey).child("MessageKeys").updateChildren(MessageKeys);

                //delete text writed in EditText
                etMessage.setText(null);
            }
        });
    }

    private void ShowData() {
        DBref.refMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<ChatMessage> ArrMessages = new ArrayList<ChatMessage>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChatMessage GroupMessage = data.getValue(ChatMessage.class);
                    String GroupMessageKey = data.getValue(ChatMessage.class).getMessageKey();
                    for (String MessageKey : MessageKeys.keySet()) {
                        if (MessageKey.equals(GroupMessageKey))
                            ArrMessages.add(GroupMessage);
                    }
                }
                ChatAdpter adpt = new ChatAdpter(Chat.this, 0, 0, ArrMessages);
                lv4.setAdapter(adpt);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
