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
import com.example.partymaker.data.firebase.DBRef;
import com.example.partymaker.data.model.ChatMessage;
import com.example.partymaker.utilities.AuthHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Adapter for displaying chat messages in a ListView for group chat. Handles message alignment,
 * bubble style, and user identification.
 */
public class ChatAdapter extends ArrayAdapter<ChatMessage> {
  /** The context in which the adapter is used. */
  Context context;
  /** The list of chat messages to display. */
  List<ChatMessage> messageList;

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

    String currentUser = AuthHelper.getCurrentUserEmail(context);
    boolean isMine = temp.getMessageUser() != null && currentUser != null && temp.getMessageUser().equals(currentUser);

    LinearLayout bubbleLayout = view.findViewById(R.id.bubbleLayout);
    TextView tvSender = view.findViewById(R.id.tvSender);
    TextView tvTime = view.findViewById(R.id.tvTime);
    TextView tvMessage = view.findViewById(R.id.messageText);
    Space spaceLeft = view.findViewById(R.id.spaceLeft);
    Space spaceRight = view.findViewById(R.id.spaceRight);

    // Handle message text, checking for null or complex content
    String messageText = temp.getMessageText();
    if (messageText == null) {
      // Try to get content from messageContent if available
      HashMap<String, Object> messageContent = temp.getMessageContent();
      if (messageContent != null) {
        // Try to extract text from the HashMap
        Object textObj = messageContent.get("text");
        if (textObj instanceof String) {
          messageText = (String) textObj;
        } else {
          messageText = "[Complex message content]";
        }
      } else {
        messageText = "[No message content]";
      }
    }
    tvMessage.setText(messageText);

    // Handle time display with null check
    if (temp.getMessageTime() != null && temp.getMessageTime().length() >= 16) {
      tvTime.setText(temp.getMessageTime().substring(11, 16)); // show only HH:mm
    } else {
      tvTime.setText("");
    }

    if (isMine) {
      // My messages: right alignment, green bubble, no name
      bubbleLayout.setBackgroundResource(R.drawable.msg_bg_bubble_mine);
      tvSender.setVisibility(View.GONE);
      spaceLeft.setVisibility(View.VISIBLE);
      spaceRight.setVisibility(View.GONE);
      tvMessage.setTextColor(ContextCompat.getColor(context, R.color.black));
    } else {
      // Messages from others: left alignment, gray bubble, with name
      bubbleLayout.setBackgroundResource(R.drawable.msg_bg_bubble);
      tvSender.setText(temp.getMessageUser() != null ? temp.getMessageUser() : "Unknown");
      tvSender.setVisibility(View.VISIBLE);
      spaceLeft.setVisibility(View.GONE);
      spaceRight.setVisibility(View.VISIBLE);
      tvMessage.setTextColor(ContextCompat.getColor(context, R.color.black));
    }

    // Add spaces between messages
    if (position > 0) {
      ChatMessage prevMessage = messageList.get(position - 1);
      if (prevMessage.getMessageUser() != null
          && temp.getMessageUser() != null
          && prevMessage.getMessageUser().equals(temp.getMessageUser())) {
        // If it's the same user, reduce the interval.
        view.setPadding(0, 2, 0, 0);
      } else {
        // If it's a different user, increase the interval.
        view.setPadding(0, 8, 0, 0);
      }
    }

    return view;
  }
}
