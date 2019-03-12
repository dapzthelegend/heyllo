package com.gamecodeschool.heyllo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton btnSendMessage;
    private EditText txtUserMessage;
    private ScrollView mScrollView;
    private TextView txtDisplayMessage;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference mDataBaseReference, groupNameRef, GroupMessageRefKEY;
    private final List<GroupMessages> groupMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager ;
    private GroupMessagesAdapter groupMessagesAdapter;
    private RecyclerView groupMessagesRecyclerList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        //Toast.makeText(GroupChatActivity.this, currentGroupName, )

        //get FireBase auth
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mDataBaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
       // GetUserInfo();
        InitializeFields();




        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                txtUserMessage.setText("");
                //Toast.makeText(GroupChatActivity.this, "Please write message first...", Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        //to display old messages when a group is selcted from the group fragment
        //create on start and set a addchild event listrener on the group name reference
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    //if that group exists, call the diplay message method

                    GroupMessages groupMessages = dataSnapshot.getValue(GroupMessages.class);
                    groupMessagesList.add(groupMessages);

                    groupMessagesAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
              //if a message is added to the child display the message
                //we get the message from the data base using the datasnapshot


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



    private void InitializeFields() {
        //GetUserInfo();
        mToolbar = (Toolbar) findViewById(R.id.groupChatBarLayout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);


        btnSendMessage = (ImageButton) findViewById(R.id.btnSendMessage);
        txtUserMessage = (EditText) findViewById(R.id.inputGroupMessage);


        mDataBaseReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();


                   // GetUserName(currentUserName);
                    boolean state = false;
                    while (state == false) {

                        if (!(currentUserName.equals(null))) {

                            groupMessagesAdapter = new GroupMessagesAdapter(groupMessagesList, currentGroupName, currentUserName);
                            groupMessagesRecyclerList = (RecyclerView) findViewById(R.id.groupMessagesRecyclerView);
                            linearLayoutManager = new LinearLayoutManager(GroupChatActivity.this);
                            groupMessagesRecyclerList.setLayoutManager(linearLayoutManager);
                            groupMessagesRecyclerList.setAdapter(groupMessagesAdapter);

                            state = true;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });






    }
    private void GetUserInfo() {
        mDataBaseReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();

                    GetUserName(currentUserName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void GetUserName(String currentUserName) {
        this.currentUserName = currentUserName;

    }

    private String SendUserName(){

        return  this.currentUserName;

    }

    private void SaveMessageInfoToDatabase() {
        String messageKEY = groupNameRef.push().getKey();
        String message = txtUserMessage.getText().toString();
        if (TextUtils.isEmpty(message)){
            Toast.makeText(GroupChatActivity.this, "Please write message first...", Toast.LENGTH_SHORT).show();


        }
        //when user clicks send, get the current time and display the time
        else{
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(callForDate.getTime());

            //call For the time
            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(callForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();

            groupNameRef.updateChildren(groupMessageKey);


            GroupMessageRefKEY = groupNameRef.child(messageKEY);

            HashMap<String, Object>  messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            GroupMessageRefKEY.updateChildren(messageInfoMap);



        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        //we use an iteratror the read each message in the group
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            txtDisplayMessage.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "   " + chatDate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

}
