package com.example.partymaker.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.example.partymaker.R;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.utils.auth.AuthenticationManager;
import com.example.partymaker.utils.core.AppConstants;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter for displaying chat messages in a ListView for group chat. Handles message alignment,
 * bubble style, and user identification.
 */
public class ChatAdapter extends ArrayAdapter<ChatMessage> {
  
  private static final String TAG = "ChatAdapter";
  
  // Message content constants
  private static final String DEFAULT_SENDER_NAME = "Unknown";
  private static final String COMPLEX_MESSAGE_PLACEHOLDER = "[Complex message content]";
  private static final String EMPTY_MESSAGE_PLACEHOLDER = "[No message content]";
  private static final String MESSAGE_CONTENT_KEY = "text";
  
  /** The context in which the adapter is used. */
  final Context context;

  /** The list of chat messages to display. */
  final List<ChatMessage> messageList;

  /**
   * Constructor for ChatAdapter.
   *
   * @param context the context
   * @param resource the layout resource ID
   * @param textViewResourceId the text view resource ID
   * @param messageList the list of chat messages
   */
  public ChatAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<ChatMessage> messageList) {
    super(context, resource, textViewResourceId, messageList);
    this.context = context;
    this.messageList = messageList;
  }

  /**
   * Constructor for ChatAdapter without textViewResourceId.
   *
   * @param context the context
   * @param resource the layout resource ID
   * @param messageList the list of chat messages
   */
  public ChatAdapter(
      @NonNull Context context, @LayoutRes int resource, @NonNull List<ChatMessage> messageList) {
    super(context, resource, messageList);
    this.context = context;
    this.messageList = messageList;
  }

  /**
   * Add a single message to the adapter and refresh the view.
   *
   * @param message The message to add
   */
  public void addMessage(ChatMessage message) {
    if (message != null) {
      this.messageList.add(message);
      notifyDataSetChanged();
    }
  }

  /**
   * Returns the view for a specific message in the list. Handles alignment, bubble style, and user
   * identification.
   *
   * @param position the position in the list
   * @param convertView the recycled view
   * @param parent the parent view group
   * @return the view for the message
   */
  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder")
    View view = layoutInflater.inflate(R.layout.item_chat_message, parent, false);
    ChatMessage temp = messageList.get(position);

    String currentUser = AuthenticationManager.getCurrentUserEmail(context);
    // Convert to the same format as messageUser (dots replaced with spaces)
    String currentUserKey = currentUser != null ? currentUser.replace('.', ' ') : null;
    boolean isMine = temp.getMessageUser() != null && temp.getMessageUser().equals(currentUserKey);

    LinearLayout bubbleLayout = view.findViewById(R.id.bubbleLayout);
    TextView tvSender = view.findViewById(R.id.tvSender);
    TextView tvTime = view.findViewById(R.id.tvTime);
    TextView tvMessage = view.findViewById(R.id.messageText);
    Space spaceLeft = view.findViewById(R.id.spaceLeft);
    Space spaceRight = view.findViewById(R.id.spaceRight);

    // Handle message text, checking for null or complex content
    String messageText = getString(temp);
    tvMessage.setText(messageText);

    // Handle time display with null check
    displayMessageTime(temp, tvTime);

    // Style message bubble based on sender
    styleMessageBubble(isMine, bubbleLayout, tvSender, spaceLeft, spaceRight, tvMessage, tvTime, temp);

    // Add spaces between messages
    setMessagePadding(position, view, temp);

    return view;
  }

  /**
   * Extracts message text from ChatMessage with fallback handling.
   *
   * @param message The ChatMessage object
   * @return The message text or a placeholder if not available
   */
  @NonNull
  private String getString(ChatMessage message) {
    String messageText = message.getMessageText();
    if (messageText == null) {
      return extractMessageFromContent(message.getMessageContent());
    }
    return messageText;
  }
  
  /**
   * Extracts message text from message content HashMap.
   *
   * @param messageContent The message content HashMap
   * @return The extracted message text or placeholder
   */
  @NonNull
  private String extractMessageFromContent(HashMap<String, Object> messageContent) {
    if (messageContent != null) {
      Object textObj = messageContent.get(MESSAGE_CONTENT_KEY);
      if (textObj instanceof String) {
        return (String) textObj;
      } else {
        return COMPLEX_MESSAGE_PLACEHOLDER;
      }
    } else {
      return EMPTY_MESSAGE_PLACEHOLDER;
    }
  }
  
  /**
   * Displays the message time with proper formatting.
   *
   * @param message The ChatMessage object
   * @param tvTime The TextView to display the time
   */
  private void displayMessageTime(ChatMessage message, TextView tvTime) {
    if (message.getMessageTime() != null && message.getMessageTime().length() >= AppConstants.Validation.TIME_SUBSTRING_END) {
      tvTime.setText(message.getMessageTime().substring(
          AppConstants.Validation.TIME_SUBSTRING_START, 
          AppConstants.Validation.TIME_SUBSTRING_END)); // show only HH:mm
    } else {
      tvTime.setText("");
    }
  }
  
  /**
   * Styles the message bubble based on whether it's from current user.
   *
   * @param isMine Whether the message is from current user
   * @param bubbleLayout The bubble layout view
   * @param tvSender The sender name TextView
   * @param spaceLeft Left space view
   * @param spaceRight Right space view
   * @param tvMessage Message text view
   * @param tvTime Time text view
   * @param message The ChatMessage object
   */
  private void styleMessageBubble(boolean isMine, LinearLayout bubbleLayout, TextView tvSender, 
      Space spaceLeft, Space spaceRight, TextView tvMessage, TextView tvTime, ChatMessage message) {
    if (isMine) {
      styleMyMessage(bubbleLayout, tvSender, spaceLeft, spaceRight, tvMessage, tvTime);
    } else {
      styleOtherMessage(bubbleLayout, tvSender, spaceLeft, spaceRight, tvMessage, tvTime, message);
    }
  }
  
  /**
   * Styles messages from current user.
   */
  private void styleMyMessage(LinearLayout bubbleLayout, TextView tvSender, 
      Space spaceLeft, Space spaceRight, TextView tvMessage, TextView tvTime) {
    // My messages: right alignment, green bubble, no name
    bubbleLayout.setBackgroundResource(R.drawable.msg_bg_bubble_mine);
    tvSender.setVisibility(View.GONE);
    spaceLeft.setVisibility(View.VISIBLE);
    spaceRight.setVisibility(View.GONE);
    tvMessage.setTextColor(ContextCompat.getColor(context, R.color.black));
    tvTime.setTextColor(ContextCompat.getColor(context, R.color.gray));
  }
  
  /**
   * Styles messages from other users.
   */
  private void styleOtherMessage(LinearLayout bubbleLayout, TextView tvSender, 
      Space spaceLeft, Space spaceRight, TextView tvMessage, TextView tvTime, ChatMessage message) {
    // Messages from others: left alignment, gray bubble, with name
    bubbleLayout.setBackgroundResource(R.drawable.msg_bg_bubble);
    tvSender.setText(message.getMessageUser() != null ? message.getMessageUser() : DEFAULT_SENDER_NAME);
    tvSender.setVisibility(View.VISIBLE);
    spaceLeft.setVisibility(View.GONE);
    spaceRight.setVisibility(View.VISIBLE);
    tvMessage.setTextColor(ContextCompat.getColor(context, R.color.black));
    tvTime.setTextColor(ContextCompat.getColor(context, R.color.gray));
  }
  
  /**
   * Sets appropriate padding between messages based on sender.
   *
   * @param position The position in the message list
   * @param view The message view
   * @param currentMessage The current message
   */
  private void setMessagePadding(int position, View view, ChatMessage currentMessage) {
    if (position > 0) {
      ChatMessage prevMessage = messageList.get(position - 1);
      boolean isSameSender = prevMessage.getMessageUser() != null
          && currentMessage.getMessageUser() != null
          && prevMessage.getMessageUser().equals(currentMessage.getMessageUser());
      
      if (isSameSender) {
        // If it's the same user, reduce the interval
        view.setPadding(0, AppConstants.UI.MESSAGE_PADDING_SAME_USER, 0, 0);
      } else {
        // If it's a different user, increase the interval
        view.setPadding(0, AppConstants.UI.MESSAGE_PADDING_DIFFERENT_USER, 0, 0);
      }
    }
  }
}
