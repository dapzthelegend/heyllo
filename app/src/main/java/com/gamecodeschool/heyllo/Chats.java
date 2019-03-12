package com.gamecodeschool.heyllo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chats extends Fragment {
    private View mView;
    private RecyclerView chatsList;
    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;




    public Chats() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
       chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
       mView =  inflater.inflate(R.layout.fragment_chats, container, false);
       chatsList = (RecyclerView) mView.findViewById(R.id.chatsList);
       chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

       return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                      final String userIDs = getRef(position).getKey();
                        final String[] retrieveImage = {"default_image"};
                      usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             if(dataSnapshot.exists()){
                                 if (dataSnapshot.hasChild("image")){
                                    retrieveImage[0] = dataSnapshot.child("image").getValue().toString();

                                     Picasso.get().load(retrieveImage[0]).into(holder.profileImage);
                                 }

                                 final  String retrieveName = dataSnapshot.child("name").getValue().toString();
                                 final  String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                                 holder.profileName.setText(retrieveName);
                                 //last seen

                                  if(dataSnapshot.child("userState").hasChild("state")){
                                     String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                      String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                      String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                            if(state.equals("online")){
                                                holder.profileStatus.setText("online");
                                            }
                                            else if(state.equals("offline")){
                                                holder.profileStatus.setText("offline");
                                                holder.profileStatus.setText("Last Seen: "  +time + " " +date );

                                            }

                                  }

                                  else{
                                      holder.profileStatus.setText("offline");

                                  }

                                 holder.itemView.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                         chatIntent.putExtra("visitUserId",userIDs);
                                         chatIntent.putExtra("visitUserName",retrieveName);
                                         chatIntent.putExtra("visitUserImage", retrieveImage[0]);

                                         startActivity(chatIntent);
                                     }
                                 });
                             }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {

                          }
                      });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                      View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                      return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView profileName, profileStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.userProfileImage);
            profileName = itemView.findViewById(R.id.userProfileName);
            profileStatus = itemView.findViewById(R.id.userProfileStatus);

        }
    }
}
