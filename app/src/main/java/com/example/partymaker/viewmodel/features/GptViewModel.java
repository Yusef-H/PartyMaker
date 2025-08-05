package com.example.partymaker.viewmodel.features;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.api.OpenAiApi;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.utils.infrastructure.system.ThreadUtils;
import com.example.partymaker.utils.security.core.SecureConfigManager;
import com.example.partymaker.viewmodel.BaseViewModel;
import java.util.ArrayList;
import java.util.List;

/** ViewModel for GptChatActivity handling GPT chat interactions */
public class GptViewModel extends BaseViewModel {

  private final MutableLiveData<List<ChatMessage>> chatHistory =
      new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<String> currentResponse = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isTyping = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isApiConfigured = new MutableLiveData<>(false);

  private OpenAiApi openAiApi;

  public GptViewModel(@NonNull Application application) {
    super(application);
    initializeOpenAiApi();
  }

  private void initializeOpenAiApi() {
    try {
      SecureConfigManager config = SecureConfigManager.getInstance(getApplication());
      String apiKey = config.getOpenAiApiKey();

      if (apiKey != null && !apiKey.isEmpty()) {
        openAiApi = new OpenAiApi(apiKey);
        isApiConfigured.setValue(true);
      } else {
        isApiConfigured.setValue(false);
        setError("OpenAI API key not configured. Please set it in settings.");
      }
    } catch (Exception e) {
      isApiConfigured.setValue(false);
      setError("Failed to load API configuration: " + e.getMessage());
    }
  }

  public LiveData<List<ChatMessage>> getChatHistory() {
    return chatHistory;
  }

  public LiveData<String> getCurrentResponse() {
    return currentResponse;
  }

  public LiveData<Boolean> getIsTyping() {
    return isTyping;
  }

  public void sendMessage(String message, String userKey, String username) {
    if (!Boolean.TRUE.equals(isApiConfigured.getValue()) || openAiApi == null) {
      setError("OpenAI API is not configured. Please check settings.");
      return;
    }

    if (message == null || message.trim().isEmpty()) {
      setError("Message cannot be empty");
      return;
    }

    // Add user message to chat history
    ChatMessage userMessage = new ChatMessage();
    userMessage.setMessage(message);
    userMessage.setSenderKey(userKey);
    userMessage.setSenderName(username);
    userMessage.setTimestamp(System.currentTimeMillis());

    List<ChatMessage> history = chatHistory.getValue();
    if (history == null) {
      history = new ArrayList<>();
    }
    history.add(userMessage);
    chatHistory.setValue(history);

    // Get GPT response
    isTyping.setValue(true);
    setLoading(true);

    ThreadUtils.runInBackground(
        () -> {
          try {
            String response = openAiApi.sendMessage(message);

            ThreadUtils.runOnMainThread(
                () -> {
                  isTyping.setValue(false);
                  setLoading(false);

                  // Add GPT response to chat history
                  ChatMessage gptMessage = new ChatMessage();
                  gptMessage.setMessage(response);
                  gptMessage.setSenderKey("gpt");
                  gptMessage.setSenderName("PartyMaker AI");
                  gptMessage.setTimestamp(System.currentTimeMillis());

                  List<ChatMessage> updatedHistory = chatHistory.getValue();
                  if (updatedHistory != null) {
                    updatedHistory.add(gptMessage);
                    chatHistory.setValue(updatedHistory);
                  }

                  currentResponse.setValue(response);
                });
          } catch (Exception e) {
            ThreadUtils.runOnMainThread(
                () -> {
                  isTyping.setValue(false);
                  setLoading(false);
                  setError("Failed to get response: " + e.getMessage());
                });
          }
        });
  }

  public void getGptResponse(String groupGoal) {
    final String finalGroupGoal =
        (groupGoal == null || groupGoal.trim().isEmpty()) ? "general party" : groupGoal;

    setLoading(true);

    ThreadUtils.runInBackground(
        () -> {
          try {
            String response = openAiApi.sendMessage("Generate party ideas for: " + finalGroupGoal);

            ThreadUtils.runOnMainThread(
                () -> {
                  setLoading(false);
                  currentResponse.setValue(response);

                  // Add to chat history
                  ChatMessage gptMessage = new ChatMessage();
                  gptMessage.setMessage(response);
                  gptMessage.setSenderKey("gpt");
                  gptMessage.setSenderName("PartyMaker AI");
                  gptMessage.setTimestamp(System.currentTimeMillis());

                  List<ChatMessage> history = chatHistory.getValue();
                  if (history == null) {
                    history = new ArrayList<>();
                  }
                  history.add(gptMessage);
                  chatHistory.setValue(history);
                });
          } catch (Exception e) {
            ThreadUtils.runOnMainThread(
                () -> {
                  setLoading(false);
                  setError("Failed to get party idea: " + e.getMessage());
                });
          }
        });
  }

  public void clearChat() {
    chatHistory.setValue(new ArrayList<>());
    currentResponse.setValue(null);
  }
}
