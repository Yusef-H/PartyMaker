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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {
  Context context;
  List<ChatMessage> MessageList;

  public ChatAdapter(
      @NonNull Context context,
      @LayoutRes int resource,
      @IdRes int textViewResourceId,
      @NonNull List<ChatMessage> MessageList) {
    super(context, resource, textViewResourceId, MessageList);
    this.context = context;
    this.MessageList = MessageList;
  }

  /** Constructor that takes Context, resource ID, and a List of ChatMessages */
  public ChatAdapter(
      @NonNull Context context, @LayoutRes int resource, @NonNull List<ChatMessage> MessageList) {
    super(context, resource, MessageList);
    this.context = context;
    this.MessageList = MessageList;
  }

  /**
   * Add a single message to the adapter
   *
   * @param message The message to add
   */
  public void addMessage(ChatMessage message) {
    if (message != null) {
      this.MessageList.add(message);
      notifyDataSetChanged();
    }
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
    @SuppressLint("ViewHolder")
    View view = layoutInflater.inflate(R.layout.item_chat_message, parent, false);
    ChatMessage temp = MessageList.get(position);

    String currentUser = Objects.requireNonNull(DBRef.Auth.getCurrentUser()).getEmail();
    boolean isMine = temp.getMessageUser() != null && temp.getMessageUser().equals(currentUser);

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
      ChatMessage prevMessage = MessageList.get(position - 1);
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
