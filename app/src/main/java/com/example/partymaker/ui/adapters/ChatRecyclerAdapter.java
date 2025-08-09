package com.example.partymaker.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.utils.auth.AuthenticationManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.MessageViewHolder> {

  private final Context context;
  private List<ChatMessage> messages;
  private String currentUserKey;

  public ChatRecyclerAdapter(Context context) {
    this.context = context;
    this.messages = new ArrayList<>();
    try {
      currentUserKey = AuthenticationManager.getCurrentUserKey(context);
    } catch (Exception e) {
      currentUserKey = null;
    }
  }

  public void setMessages(List<ChatMessage> messages) {
    this.messages = messages != null ? messages : new ArrayList<>();
    notifyDataSetChanged();
  }

  public void addMessage(ChatMessage message) {
    messages.add(message);
    notifyItemInserted(messages.size() - 1);
  }

  public void clear() {
    messages.clear();
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
    return new MessageViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    ChatMessage message = messages.get(position);
    
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
    return messages.size();
  }

  static class MessageViewHolder extends RecyclerView.ViewHolder {
    View sentMessageLayout;
    View receivedMessageLayout;
    TextView tvSentMessage;
    TextView tvSentTime;
    TextView tvReceivedMessage;
    TextView tvSenderName;
    TextView tvReceivedTime;

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
  }
}