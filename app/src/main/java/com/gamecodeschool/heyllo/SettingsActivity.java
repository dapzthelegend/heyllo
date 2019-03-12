package com.gamecodeschool.heyllo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button btnUpdateSettings;
    private EditText txtUserName, txtUserStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference mDataBaseReference;
    private static final  int REQUEST_CODE= 1;
    //reference to store the image
    private StorageReference userImagesReference;
    private ProgressDialog mProgressDialog;
    private String downloadUrl;
   private String profileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        //initialize the firebase storage
        userImagesReference = FirebaseStorage.getInstance().getReference().child("Profile Images");



        InitializeFields();
        btnUpdateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
        RetrieveUserInformation();

        //setting the profile Image
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_CODE);
            }
        });


    }


    private void InitializeFields() {
        btnUpdateSettings = (Button) findViewById(R.id.btnUpdateSettings);
        txtUserName = (EditText) findViewById(R.id.setUserName);
        txtUserStatus = (EditText) findViewById(R.id.setProfileStatus);
        userProfileImage = (CircleImageView) findViewById(R.id.profileImage);
        mProgressDialog = new ProgressDialog(this);

    }

    private void UpdateSettings() {


        mDataBaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())  && (dataSnapshot.hasChild("image"))) {

                    profileImage = dataSnapshot.child("image").getValue().toString();
                        //Log.e("image", profileImage);
                    Log.e("image", profileImage);


                        }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });


        String username = txtUserName.getText().toString();
        String status = txtUserStatus.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(SettingsActivity.this, "write your username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(SettingsActivity.this, "write your status", Toast.LENGTH_SHORT).show();

        } else {
            //when you update the settings, add the status and username to the databse
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", username);
            profileMap.put("status", status);
           int i = 0;
            while(i<200){
                if(profileImage != null){
                    profileMap.put("image", profileImage);
                    Log.e("image", " " + "1: " + profileImage);
                      i = 200;
                }
                i++;
                Log.e("image", " " + "while: " + profileImage);
            }


            mDataBaseReference.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Log.e("find friends" , "sending users  to friends");


                                //if the profile is updated successfully display the message
                                //Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();


                            } else {
                                //if creating the profile isn't successful, do this
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();


                            }


                        }

                    });
            //this methods gets the users profile from the databse and displays it


        }




    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
       // mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);



    }


    private void RetrieveUserInformation() {

     mDataBaseReference.child("Users").child(currentUserID)
             .addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                         String username = dataSnapshot.child("name").getValue().toString();
                         String status = dataSnapshot.child("status").getValue().toString();
                         String profileImage = dataSnapshot.child("image").getValue().toString();

                         txtUserName.setText(username);
                         txtUserStatus.setText(status);
                         Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(userProfileImage);




                     }



                     else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                         String username = dataSnapshot.child("name").getValue().toString();
                         String status = dataSnapshot.child("status").getValue().toString();

                         txtUserName.setText(username);
                         txtUserStatus.setText(status);


                     }
                     else{
                         Toast.makeText(SettingsActivity.this, "set profile information", Toast.LENGTH_SHORT).show();

                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                //TO store the cropped image into a firebase storage
                if(resultCode == RESULT_OK){
                    mProgressDialog.setTitle("set profile image");
                    mProgressDialog.setMessage("please wait while profile image is being updated");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    final Uri resultUri = result.getUri();

                    //create a storage reference to the path which image is to be stored

                    final StorageReference filepath = userImagesReference.child(currentUserID+".jpg");
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){

                              filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                  @Override
                                  public void onSuccess(Uri uri) {

                                   downloadUrl = uri.toString();
                                   if (downloadUrl != null){
                                       mDataBaseReference.child("Users").child(currentUserID).child("image")
                                               .setValue(downloadUrl)
                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                       if (task.isSuccessful()){
                                                           Toast.makeText(SettingsActivity.this, "Profile Image Saved", Toast.LENGTH_SHORT).show();
                                                           mProgressDialog.dismiss();
                                                       }

                                                       else{
                                                           String message = task.getException().toString();
                                                           Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                                                           mProgressDialog.dismiss();
                                                       }
                                                   }
                                               });
                                   }

                                  }
                              });


                            }

                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                           mProgressDialog.dismiss();
                            }
                        }
                    });

                }
                }

    }

    private void SendUserToFindFriends(){
        Intent findIntent = new Intent(SettingsActivity.this, FindFriendsActivity.class);
        findIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(findIntent);
                finish();

    }

}
