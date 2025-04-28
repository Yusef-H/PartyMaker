package com.example.partymaker.data;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.example.partymaker.R;

import java.util.List;

public class ChatAdpter extends ArrayAdapter<ChatMessage> {
    Context context;
    List<ChatMessage> MessageList;

    public ChatAdpter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<ChatMessage> MessageList) {
        super(context, resource, textViewResourceId, MessageList);
        this.context = context;
        this.MessageList = MessageList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.messages_list, parent, false);
        ChatMessage temp = MessageList.get(position);

        TextView tvpUserName = (TextView) view.findViewById(R.id.tvMsgUser);
        tvpUserName.setText(temp.getMessageUser());

        TextView tvpDate = (TextView) view.findViewById(R.id.tvMsgDate);
        tvpDate.setText(temp.getMessageTime());

        TextView tvpText = (TextView) view.findViewById(R.id.tvMsgText);
        tvpText.setText(temp.getMessageText());

        //if you want to set pics back unNote this code and xml code in messages_list
        /*/

        final ImageView imageView = (ImageView) view.findViewById(R.id.imgMLprofile);

        String UserImageProfile=temp.getMessageUser();
        String email=UserImageProfile.replace('.',' ');

        DBref.refStorage.child("Users/"+email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context)
                        .load(uri) // image url goes here
                        .fit()
                        .centerCrop()
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        /*/

        return view;
    }
}
