package com.gamecodeschool.heyllo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerList;
    private DatabaseReference mUserRef;
    private DatabaseReference mDatebaseReference;
    private String currentUserID;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mRecyclerList = (RecyclerView) findViewById(R.id.findFriendsRecyclerList);
        mRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mDatebaseReference = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = (Toolbar) findViewById(R.id.findFriendsToolBar);
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(mUserRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder > adapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {
                final String userIDs = getRef(position).getKey();

                mUserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            if(dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                if(state.equals("online")){
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline")){
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);

                                }

                            }

                            else{
                                holder.onlineIcon.setVisibility(View.INVISIBLE);

                            }


                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
               holder.userName.setText(model.getName());
               holder.userStatus.setText(model.getStatus());
               Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.userImage);

               //to get user profile when clicked on in the recycler view
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visitUser = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("visitUser", visitUser);
                        startActivity(profileIntent);

                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }


    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView userImage;
        ImageView onlineIcon;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.userProfileName);
            userStatus = (TextView) itemView.findViewById(R.id.userProfileStatus);
            userImage= (CircleImageView) itemView.findViewById(R.id.userProfileImage);
            onlineIcon = (ImageView) itemView.findViewById(R.id.userOnlineStatus);

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
    protected void onStop() {
        super.onStop();
        updateUserStatus("offline");
    }
}
