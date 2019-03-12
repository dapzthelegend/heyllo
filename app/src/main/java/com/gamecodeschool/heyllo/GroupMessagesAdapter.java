package com.gamecodeschool.heyllo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.GroupMessagesViewHolder> {


    private List<GroupMessages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    public String groupName;
    private String userName;

    public GroupMessagesAdapter (List<GroupMessages> groupMessagesList, String groupName, String userName){
        this.groupMessagesList = groupMessagesList;
        this.groupName = groupName;
        this.userName = userName;

    }

    public class GroupMessagesViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;

        public GroupMessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sentText);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receivedText);


        }
    }




    @NonNull
    @Override
    public GroupMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_massages_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new GroupMessagesViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull final GroupMessagesViewHolder groupMessagesViewHolder,  int i) {
       String messageSenderId = mAuth.getCurrentUser().getUid();

                   GroupMessages groupMessages = groupMessagesList.get(i);

                   String fromUserName = groupMessages.getName();
                   String time = groupMessages.getTime();

                   usersRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);


                 groupMessagesViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);


            if (fromUserName.equals(userName)) {
                groupMessagesViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
               groupMessagesViewHolder.senderMessageText.setText(groupMessages.getMessage() + "\n               " + time);

            } else {
                groupMessagesViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                groupMessagesViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                groupMessagesViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                groupMessagesViewHolder.receiverMessageText.setText(fromUserName + "\n" + groupMessages.getMessage() + "\n            " + time);

          }
        }









    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }



}
