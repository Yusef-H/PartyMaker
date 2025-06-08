package com.example.partymaker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.LocaleList;                  // NEW
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GptChatActivity extends AppCompatActivity {

    // ---------- Views ----------
    private RecyclerView chatRecyclerView;
    private EditText messageInput;

    // ---------- Lists ----------
    // רק מה שמוצג למשתמש
    private final List<SimpleChatMessage> visibleMessages = new ArrayList<>();    // NEW
    // כל ההיסטוריה שנשלחת ל-API (כולל system)
    private final List<SimpleChatMessage> history = new ArrayList<>();    // NEW

    private ChatAdapter2 chatAdapter;
    private OpenAiApi openAiApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ---------- System prompt ----------
    private static final SimpleChatMessage SYSTEM_PROMPT =                        // NEW
            new SimpleChatMessage("system", "ענה תמיד בעברית, גם אם השאלה באנגלית.");

    // ------------------------------------------------------------------------
    // onCreate
    // ------------------------------------------------------------------------
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_dialog);

        // ---------- Init OpenAI helper ----------
        String apiKey = getApiKey();
        openAiApi = new OpenAiApi(apiKey);

        // ---------- Hide action bar (only this activity) ----------
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // ---------- Init views ----------
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);
        MaterialToolbar toolbar = findViewById(R.id.chatToolbar);

        // Toolbar back button
        toolbar.setNavigationOnClickListener(v -> finish());

        // ---------- RecyclerView ----------
        chatAdapter = new ChatAdapter2(visibleMessages);                           // CHANGED
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // ---------- Add system prompt (לא מוצג) ----------
        history.add(SYSTEM_PROMPT);                                                // NEW

        // ---------- Assistant welcome (כן מוצג) ----------
        SimpleChatMessage welcome = new SimpleChatMessage(
                "assistant",
                "🎉 ברוכים הבאים לעזרה באפליקציית PartyMaker – האפליקציה המושלמת לתכנון מסיבות!\n\n" +
                        "אני כאן כדי לעזור לך בכל שאלה או בעיה. שאל/י אותי איך מוסיפים חברים, יוצרים קבוצה, מנהלים אירוע, או כל דבר אחר – ואסביר לך שלב-אחר-שלב בעברית.\n\n" +
                        "איך אפשר לעזור?");
        history.add(welcome);
        visibleMessages.add(welcome);                                              // NEW
        chatAdapter.notifyDataSetChanged();

        // ---------- Keyboard hint to Hebrew ----------
        messageInput.setImeHintLocales(new LocaleList(new Locale("he")));

        // ---------- Send button ----------
        sendButton.setOnClickListener(v -> {
            String userText = messageInput.getText().toString().trim();
            if (!userText.isEmpty()) {
                sendMessage(userText);
                messageInput.setText("");
            }
        });
    }

    // ------------------------------------------------------------------------
    // Get API key from assets/local.properties
    // ------------------------------------------------------------------------
    private String getApiKey() {
        try (InputStream inputStream = getAssets().open("local.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("OPENAI_API_KEY", "");
        } catch (IOException e) {
            Toast.makeText(this, "API key error", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    // ------------------------------------------------------------------------
    // Send user message
    // ------------------------------------------------------------------------
    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage(String userText) {
        // ---------- User message ----------
        SimpleChatMessage userMsg = new SimpleChatMessage("user", userText);
        visibleMessages.add(userMsg);       // מציג
        history.add(userMsg);               // להיסטוריה
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(visibleMessages.size() - 1);

        // ---------- Call OpenAI ----------
        executor.execute(() -> {
            try {
                String answer = openAiApi.sendMessageWithHistory(history);

                SimpleChatMessage assistantMsg =
                        new SimpleChatMessage("assistant", answer);

                runOnUiThread(() -> {
                    history.add(assistantMsg);          // להמשך קונטקסט
                    visibleMessages.add(assistantMsg);  // להצגה
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(visibleMessages.size() - 1);
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }
        });
    }
}
