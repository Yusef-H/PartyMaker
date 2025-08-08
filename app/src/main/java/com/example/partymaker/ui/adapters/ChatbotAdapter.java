package com.example.partymaker.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.partymaker.R;
import com.example.partymaker.data.model.ChatMessageGpt;
import java.util.List;

/**
 * RecyclerView Adapter for displaying GPT-based chatbot messages. Handles user/assistant message
 * styling.
 */
public class ChatbotAdapter extends RecyclerView.Adapter<ChatbotAdapter.MessageViewHolder> {
  
  // UI Constants
  private static final int MESSAGE_PADDING = 32;
  
  /** The list of chatbot messages to display. */
  private final List<ChatMessageGpt> messages;

  /**
   * Constructs the adapter with a list of messages.
   *
   * @param messages the list of chatbot messages
   */
  public ChatbotAdapter(List<ChatMessageGpt> messages) {
    this.messages = messages;
  }

  /** Inflates the view for a single message. */
  @NonNull
  @Override
  public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_chatbot_message, parent, false);
    return new MessageViewHolder(view);
  }

  /** Binds a message to the view holder, styling by role. */
  @Override
  public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
    ChatMessageGpt message = messages.get(position);
    setupMessageContent(holder, message);
    setupMessageStyling(holder, message);
  }
  
  /**
   * Sets up the message content in the view holder.
   */
  private void setupMessageContent(@NonNull MessageViewHolder holder, @NonNull ChatMessageGpt message) {
    holder.messageText.setText(message.content);
    holder.messageText.setPadding(MESSAGE_PADDING, MESSAGE_PADDING, MESSAGE_PADDING, MESSAGE_PADDING);
  }
  
  /**
   * Sets up the message styling based on role.
   */
  private void setupMessageStyling(@NonNull MessageViewHolder holder, @NonNull ChatMessageGpt message) {
    if (ChatMessageGpt.ROLE_USER.equals(message.role)) {
      holder.messageText.setBackgroundResource(R.drawable.msg_user_bg);
    } else if (ChatMessageGpt.ROLE_ASSISTANT.equals(message.role)) {
      holder.messageText.setBackgroundResource(R.drawable.msg_bg_assistant);
    } else {
      // Default styling for system or other roles
      holder.messageText.setBackgroundResource(R.drawable.msg_bg_assistant);
    }
  }

  /** Returns the number of messages in the adapter. */
  @Override
  public int getItemCount() {
    return messages.size();
  }

  /** ViewHolder for chatbot messages. */
  static class MessageViewHolder extends RecyclerView.ViewHolder {
    final TextView messageText;

    MessageViewHolder(@NonNull View itemView) {
      super(itemView);
      messageText = itemView.findViewById(R.id.messageText);
    }
  }
}
