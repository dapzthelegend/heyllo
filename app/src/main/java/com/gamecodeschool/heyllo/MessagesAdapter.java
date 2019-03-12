package com.gamecodeschool.heyllo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    public MessagesAdapter (List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sentText);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receivedText);

        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_massages_layout, viewGroup, false);
              mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
          String messageSenderID = mAuth.getCurrentUser().getUid();
          Messages messages = userMessageList.get(i);


          String fromUserID  = messages.getFrom();
          String fromMessageType = messages.getType();


          if (fromMessageType.equals("text")){
              messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
              messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);



              if (fromUserID.equals(messageSenderID)) {

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            } else {

                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());


            }
        }


    }

    @Override
    public int getItemCount() {

        return userMessageList.size();
    }



}
