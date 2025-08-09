package com.example.partymaker.ui.features.auxiliary.chatbot;

import android.os.Bundle;
import android.os.LocaleList;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.api.OpenAiApi;
import com.example.partymaker.data.model.ChatMessageGpt;
import com.example.partymaker.ui.adapters.ChatbotAdapter;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.viewmodel.features.GptViewModel;
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

  // ---------- System prompt ----------
  private static final ChatMessageGpt SYSTEM_PROMPT = // NEW
      new ChatMessageGpt(
          "system", "Always answer in English, even if the question is in another language.");
  // ---------- Lists ----------
  // Only what is displayed to the user
  private final List<ChatMessageGpt> visibleMessages = new ArrayList<>(); // NEW
  // All history sent to API (including system)
  private final List<ChatMessageGpt> history = new ArrayList<>(); // NEW
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  // ---------- Views ----------
  private RecyclerView chatRecyclerView;
  private EditText messageInput;
  private ChatbotAdapter chatAdapter;
  private OpenAiApi openAiApi;
  private GptViewModel viewModel;

  // ------------------------------------------------------------------------
  // onCreate
  // ------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_chatbot);

    // ---------- Init ViewModel ----------
    viewModel = new ViewModelProvider(this).get(GptViewModel.class);
    setupViewModelObservers();

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
    chatAdapter = new ChatbotAdapter(visibleMessages); // CHANGED
    chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    chatRecyclerView.setAdapter(chatAdapter);

    // ---------- Add system prompt (not displayed) ----------
    history.add(SYSTEM_PROMPT); // NEW

    // ---------- Assistant welcome (displayed) ----------
    ChatMessageGpt welcome =
        new ChatMessageGpt(
            "assistant",
            "🎉 Welcome to PartyMaker app help – the perfect app for planning parties!\n\n"
                + "I'm here to help you with any question or issue. Ask me how to add friends, create a group, manage an event, or anything else – and I'll explain it to you step-by-step in English.\n\n"
                + "How can I help?");
    history.add(welcome);
    visibleMessages.add(welcome); // NEW
    chatAdapter.notifyItemInserted(0);

    // ---------- Keyboard hint to English ----------
    messageInput.setImeHintLocales(new LocaleList(new Locale("en")));

    // ---------- Send button ----------
    sendButton.setOnClickListener(
        v -> {
          String userText = messageInput.getText().toString().trim();
          if (!userText.isEmpty()) {
            sendMessage(userText);
            messageInput.setText("");
          }
        });
  }

  /** Sets up observers for ViewModel LiveData */
  private void setupViewModelObservers() {
    viewModel
        .getChatHistory()
        .observe(
            this,
            messages -> {
              if (messages != null) {
                int previousSize = visibleMessages.size();
                visibleMessages.clear();
                // Convert ChatMessage to ChatMessageGpt
                for (com.example.partymaker.data.model.ChatMessage msg : messages) {
                  ChatMessageGpt gptMsg =
                      new ChatMessageGpt(
                          msg.getSenderKey().equals("gpt") ? "assistant" : "user",
                          msg.getMessage());
                  visibleMessages.add(gptMsg);
                }
                // Use more specific notify methods for better performance
                if (previousSize == 0 && !visibleMessages.isEmpty()) {
                  // First load - insert all items
                  chatAdapter.notifyItemRangeInserted(0, visibleMessages.size());
                } else if (visibleMessages.size() > previousSize) {
                  // New messages added
                  chatAdapter.notifyItemRangeInserted(
                      previousSize, visibleMessages.size() - previousSize);
                } else {
                  // Full refresh needed (rare case)
                  chatAdapter.notifyDataSetChanged();
                }
                scrollToBottom();
              }
            });

    viewModel
        .getIsTyping()
        .observe(
            this,
            isTyping -> {
              // Could show typing indicator here
            });

    viewModel
        .getIsLoading()
        .observe(
            this,
            isLoading -> {
              // Disable send button while loading
              findViewById(R.id.sendButton).setEnabled(!isLoading);
            });
  }

  private void scrollToBottom() {
    if (chatAdapter.getItemCount() > 0) {
      chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
    }
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
  private void sendMessage(String userText) {
    // ---------- User message ----------
    ChatMessageGpt userMsg = new ChatMessageGpt("user", userText);
    int insertPosition = visibleMessages.size();
    visibleMessages.add(userMsg); // display
    history.add(userMsg); // for history
    chatAdapter.notifyItemInserted(insertPosition);
    chatRecyclerView.scrollToPosition(visibleMessages.size() - 1);

    // ---------- Call OpenAI ----------
    executor.execute(
        () -> {
          try {
            String answer = openAiApi.sendMessageWithHistory(history);

            ChatMessageGpt assistantMsg = new ChatMessageGpt("assistant", answer);

            ThreadUtils.runOnMainThread(
                () -> {
                  history.add(assistantMsg); // for continued context
                  int assistantInsertPosition = visibleMessages.size();
                  visibleMessages.add(assistantMsg); // for display
                  chatAdapter.notifyItemInserted(assistantInsertPosition);
                  chatRecyclerView.scrollToPosition(visibleMessages.size() - 1);
                });

          } catch (Exception e) {
            ThreadUtils.runOnMainThread(
                () -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
          }
        });
  }
}
