package com.example.partymaker.viewmodel.groups;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/** Simplified ViewModel for ChatActivity handling chat messages */
public class GroupChatViewModel extends BaseViewModel {

  private final MutableLiveData<List<ChatMessage>> messages =
      new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<String> groupKey = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isMessageSent = new MutableLiveData<>(false);

  public GroupChatViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<List<ChatMessage>> getMessages() {
    return messages;
  }

  public LiveData<String> getGroupKey() {
    return groupKey;
  }

  public LiveData<Boolean> getIsMessageSent() {
    return isMessageSent;
  }

  public void setGroupKey(String key) {
    groupKey.setValue(key);
  }

  public void addMessage(ChatMessage message) {
    List<ChatMessage> currentMessages = messages.getValue();
    if (currentMessages == null) {
      currentMessages = new ArrayList<>();
    }
    currentMessages.add(message);
    messages.setValue(currentMessages);
    isMessageSent.setValue(true);
  }

  public void clearMessages() {
    messages.setValue(new ArrayList<>());
  }

  public void resetMessageSentFlag() {
    isMessageSent.setValue(false);
  }
}
