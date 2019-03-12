package com.gamecodeschool.heyllo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String visitUserID;

    private CircleImageView visitProfileImage;
    private TextView visitProfileName, visitProfileStatus;
    private Button btnSendMessageRequest;
    private DatabaseReference mUserRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;
    private String currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        visitUserID = getIntent().getExtras().get("visitUser").toString();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        //Toast.makeText(this, "USER ID:" + visitUserID, Toast.LENGTH_SHORT).show();
        visitProfileImage =(CircleImageView) findViewById(R.id.visitProfileImage);
        visitProfileName = (TextView) findViewById(R.id.visitProfileName);
        visitProfileStatus = (TextView) findViewById(R.id.visitProfileStatus);
        btnSendMessageRequest = (Button) findViewById(R.id.visitProfileSendRequest);
        if (visitUserID.equals(currentUserId)){
            btnSendMessageRequest.setVisibility(View.INVISIBLE);
            //Log.e("id", "now invisible");
        }



        //Log.e("id", "sender " + currentUserId + "receiver " + visitUserID );
        btnSendMessageRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactsRef.child(currentUserId).child(visitUserID)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                    if (visitUserID .equals(currentUserId)){

                                    }
                                    else {


                                        contactsRef.child(visitUserID).child(currentUserId)
                                                .child("Contacts").setValue("Saved")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            StartMainActivity();
                                                            HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                            chatNotificationMap.put("from", currentUserId);
                                                            chatNotificationMap.put("type", "request");
                                                            notificationRef.child(visitUserID).push()
                                                                    .setValue(chatNotificationMap);




                                                        }

                                                    }
                                                });
                                    }


                                }

                            }
                        });

            }
        });
       RetrieveUserInformation();

    }

    private void StartMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    private void RetrieveUserInformation() {
        mUserRef.child(visitUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(visitProfileImage);
                    visitProfileStatus.setText(userStatus);
                    visitProfileName.setText(userName);


                }

                else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    visitProfileStatus.setText(userStatus);
                    visitProfileName.setText(userName);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
