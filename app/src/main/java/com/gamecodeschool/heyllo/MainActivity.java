package com.gamecodeschool.heyllo;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAdapter;

    private FirebaseAuth mAuth, mAuth2;
    private DatabaseReference mDatebaseReference;

    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Heyllo");

        mViewPager = (ViewPager) findViewById(R.id.mainTabsPager);
        mTabsAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(mViewPager);
        //get mAuth and set Current User
        mAuth = FirebaseAuth.getInstance();


        //initialize the database regerence
        mDatebaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
       // updateUserStatus("online");
        if (currentUser == null){
            SendUserToLogingActivity();
        }

        else{

            //use this method to verify the existence of the user
            VerifyUserExistence();
            updateUserStatus("online");
        }


    }




    private void VerifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();
       mDatebaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if((dataSnapshot.child("name").exists())){



               }

               else if (!(dataSnapshot.child("name").exists())){
                   SendUserToSettingsActivity();
                   Log.e("error", "doesn't exist");


               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

    }

    private void SendUserToLogingActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //create a switch statement for the selcted menus
        switch (item.getItemId()){
            case R.id.logoutOption:
                //sign out the user
                updateUserStatus("offline");

                mAuth.signOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                SendUserToLogingActivity();
                break;

            case R.id.settingsOption:
                SendUserToSettingsActivity();
                break;

            case R.id.findFriendsOption:
                SendUserToFindFriendsActivity();
                break;



            //case create Group
            case R.id.createGroup:
                RequestNewGroup();
        }
        return true;
    }

    private void RequestNewGroup() {
        //create a dialog that asks user to enter group name
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g coding cafe");
        builder.setView(groupNameField);
        //positive button is set
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                //if the group name is empty ask the user to enter the group name
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write group name..", Toast.LENGTH_SHORT).show();
                    }
                   //if user enters group name create a group

                 else{
                    CreateNewGroup(groupName);

                }
            }
        });
        //set the negative cancel button for users
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
            }
        });
        builder.show();

    }

    private void CreateNewGroup(final String groupname) {
        mDatebaseReference.child("Groups").child(groupname).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupname + " is created successfully",  Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);

        startActivity(settingsIntent);


    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);


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
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");

    }


}
