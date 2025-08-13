package com.example.partymaker.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.utils.auth.AuthenticationManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.MessageViewHolder> {

  private final Context context;
  private final List<ChatMessage> currentMessages = new ArrayList<>();
  private String currentUserKey;

  public ChatRecyclerAdapter(Context context) {
    this.context = context;
    try {
      currentUserKey = AuthenticationManager.getCurrentUserKey(context);
    } catch (Exception e) {
      currentUserKey = null;
    }
  }

  public void updateMessages(List<ChatMessage> newMessages) {
    List<ChatMessage> messages = newMessages != null ? newMessages : new ArrayList<>();
    DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MessageDiffCallback(currentMessages, messages));
    currentMessages.clear();
    currentMessages.addAll(messages);
    result.dispatchUpdatesTo(this);
  }

  /**
   * @deprecated Use updateMessages() instead for better performance
   */
  @Deprecated
  public void setMessages(List<ChatMessage> messages) {
    updateMessages(messages);
  }

  public void addMessage(ChatMessage message) {
    currentMessages.add(message);
    notifyItemInserted(currentMessages.size() - 1);
  }

  public void clear() {
    int size = currentMessages.size();
    if (size > 0) {
      currentMessages.clear();
      notifyItemRangeRemoved(0, size);
    }
  }

  @NonNull
  @Override
  public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
    return new MessageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    ChatMessage message = currentMessages.get(position);
    
    // Get message text
    String messageText = message.getMessage();
    if (messageText == null || messageText.isEmpty()) {
      messageText = message.getMessageText();
    }
    
    // Get sender name
    String senderName = message.getSenderName();
    if (senderName == null || senderName.isEmpty()) {
      senderName = message.getMessageUser();
    }
    
    // Get time
    String timeText = "";
    if (message.getMessageTime() != null) {
      timeText = message.getMessageTime();
    } else if (message.getTimestamp() > 0) {
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
      timeText = sdf.format(new Date(message.getTimestamp()));
    }
    
    // Check if message is from current user
    String senderKey = message.getSenderKey();
    if (senderKey == null || senderKey.isEmpty()) {
      senderKey = message.getMessageUser();
    }
    
    boolean isCurrentUser = currentUserKey != null && currentUserKey.equals(senderKey);
    
    // Show/hide appropriate layouts based on message sender
    if (isCurrentUser) {
      // Show sent message layout
      holder.sentMessageLayout.setVisibility(View.VISIBLE);
      holder.receivedMessageLayout.setVisibility(View.GONE);
      holder.tvSentMessage.setText(messageText);
      holder.tvSentTime.setText(timeText);
    } else {
      // Show received message layout
      holder.sentMessageLayout.setVisibility(View.GONE);
      holder.receivedMessageLayout.setVisibility(View.VISIBLE);
      holder.tvReceivedMessage.setText(messageText);
      holder.tvSenderName.setText(senderName);
      holder.tvSenderName.setVisibility(View.VISIBLE);
      holder.tvReceivedTime.setText(timeText);
    }
  }

  @Override
  public int getItemCount() {
    return currentMessages.size();
  }

  @Override
  public void onViewRecycled(@NonNull MessageViewHolder holder) {
    super.onViewRecycled(holder);
    // Clear text views to prevent showing old data during recycling
    holder.clear();
  }

  /**
   * DiffUtil callback for efficiently comparing chat messages
   */
  private static class MessageDiffCallback extends DiffUtil.Callback {
    private final List<ChatMessage> oldList;
    private final List<ChatMessage> newList;
    
    MessageDiffCallback(List<ChatMessage> oldList, List<ChatMessage> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }
    
    @Override
    public int getOldListSize() {
      return oldList.size();
    }
    
    @Override
    public int getNewListSize() {
      return newList.size();
    }
    
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      ChatMessage oldMessage = oldList.get(oldItemPosition);
      ChatMessage newMessage = newList.get(newItemPosition);
      
      // Compare message IDs first
      String oldId = oldMessage.getMessageKey();
      String newId = newMessage.getMessageKey();
      if (oldId != null && newId != null) {
        return oldId.equals(newId);
      }
      
      // Fallback: compare timestamp and sender
      return oldMessage.getTimestamp() == newMessage.getTimestamp() &&
             Objects.equals(oldMessage.getSenderKey(), newMessage.getSenderKey());
    }
    
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      ChatMessage oldMessage = oldList.get(oldItemPosition);
      ChatMessage newMessage = newList.get(newItemPosition);
      
      return Objects.equals(oldMessage.getMessage(), newMessage.getMessage()) &&
             Objects.equals(oldMessage.getMessageText(), newMessage.getMessageText()) &&
             Objects.equals(oldMessage.getSenderName(), newMessage.getSenderName()) &&
             Objects.equals(oldMessage.getMessageUser(), newMessage.getMessageUser()) &&
             Objects.equals(oldMessage.getMessageTime(), newMessage.getMessageTime()) &&
             oldMessage.getTimestamp() == newMessage.getTimestamp();
    }
  }

  static class MessageViewHolder extends RecyclerView.ViewHolder {
    final View sentMessageLayout;
    final View receivedMessageLayout;
    final TextView tvSentMessage;
    final TextView tvSentTime;
    final TextView tvReceivedMessage;
    final TextView tvSenderName;
    final TextView tvReceivedTime;

    MessageViewHolder(@NonNull View itemView) {
      super(itemView);
      sentMessageLayout = itemView.findViewById(R.id.sentMessageLayout);
      receivedMessageLayout = itemView.findViewById(R.id.receivedMessageLayout);
      tvSentMessage = itemView.findViewById(R.id.tvSentMessage);
      tvSentTime = itemView.findViewById(R.id.tvSentTime);
      tvReceivedMessage = itemView.findViewById(R.id.tvReceivedMessage);
      tvSenderName = itemView.findViewById(R.id.tvSenderName);
      tvReceivedTime = itemView.findViewById(R.id.tvReceivedTime);
    }
    
    /**
     * Clears all text views to prevent showing old data during recycling
     */
    void clear() {
      tvSentMessage.setText("");
      tvSentTime.setText("");
      tvReceivedMessage.setText("");
      tvSenderName.setText("");
      tvReceivedTime.setText("");
      sentMessageLayout.setVisibility(View.GONE);
      receivedMessageLayout.setVisibility(View.GONE);
    }
  }
}