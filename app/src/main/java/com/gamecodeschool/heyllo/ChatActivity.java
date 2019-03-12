package com.gamecodeschool.heyllo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
   private String chatID, chatName, chatImage, messageSenderID;
   private ImageButton btnSendPrivateMessage;
   private EditText txtMessageInput;
   private String currentUserID;

   private TextView userName, userLastSeen;
   private CircleImageView mImageView;
   private Toolbar chatToolbar;
   private FirebaseAuth mAuth;
   private DatabaseReference rootRef;
   private final List<Messages> messagesList = new ArrayList<>();
   private LinearLayoutManager linearLayoutManager ;
   private MessagesAdapter messagesAdapter;
   private RecyclerView recyclerView;
    private DatabaseReference mDatebaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mDatebaseReference = FirebaseDatabase.getInstance().getReference();

        chatID = getIntent().getExtras().get("visitUserId").toString();
        chatName = getIntent().getExtras().get("visitUserName").toString();
        chatImage = getIntent().getExtras().get("visitUserImage").toString();




          InitializeControllers();

        userName.setText(chatName);
        Picasso.get().load(chatImage).placeholder(R.drawable.profile_image).into(mImageView);
        displayLastSeen();


        btnSendPrivateMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();

            }
        });
       updateUserStatus("online");

    }



    private void InitializeControllers() {

        chatToolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
        userName = (TextView) findViewById(R.id.customProfileName);
        userLastSeen = (TextView) findViewById(R.id.customProfileLastSeen);


        mImageView = (CircleImageView) findViewById(R.id.customProfileImage);

        btnSendPrivateMessage = (ImageButton) findViewById(R.id.sendMessageButton);
        txtMessageInput = (EditText) findViewById(R.id.inputMessage);

        messagesAdapter =  new MessagesAdapter(messagesList);
        recyclerView = (RecyclerView) findViewById(R.id.privateMessages);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messagesAdapter);





        }


        private void displayLastSeen(){
        rootRef.child("Users").child(chatID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                            String state = dataSnapshot.child("userState").child("state").getValue().toString();

                            if(state.equals("online")){
                                userLastSeen.setText("online");

                            }
                            else if(state.equals("offline")){
                                userLastSeen.setText("offline");


                            }





                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }

    @Override
    protected void onStart() {

        updateUserStatus("online");
        super.onStart();
        rootRef.child("Messages").child(messageSenderID).child(chatID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                messagesList.add(messages);
                messagesAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendMessage() {
        String message = txtMessageInput.getText().toString();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "please enter message..", Toast.LENGTH_SHORT).show();
        }

        else{
            String messageSenderRef = "Messages/" + messageSenderID + "/" + chatID;
            String messageReceiverRef = "Messages/" + chatID + "/" + messageSenderID;

            DatabaseReference userMessageKeyReference = rootRef.child("Messages").child(messageSenderID).child(chatID).push();

            String messagePushID = userMessageKeyReference.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);

            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
            txtMessageInput.setText("");

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "sent", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();

                    }




                }





            });





        }

    }
    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();
        mDatebaseReference.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineState);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateUserStatus("offline");

    }


    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");


    }
}
