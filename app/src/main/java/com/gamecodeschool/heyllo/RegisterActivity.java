package com.gamecodeschool.heyllo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText txtRegisterEmail, txtRegisterPassword;
    private TextView txtAlreadyHaveAnAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseReference, usersRef;
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //get Firebase auth
        mAuth = FirebaseAuth.getInstance();
        InitializeFileds();

        //initialize Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        txtAlreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this method creates a new account
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = txtRegisterEmail.getText().toString();
        String password = txtRegisterPassword.getText().toString();

        if (TextUtils.isEmpty(email)){

            Toast.makeText(this,"Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){

            Toast.makeText(this,"Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else{
            mLoadingBar.setTitle("Creating New Account");
            mLoadingBar.setMessage("Please wait while new account is created...");
            mLoadingBar.setCanceledOnTouchOutside(true);
            mLoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                final String currentUserID = mAuth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) {

                                            return;
                                        }

                                        // Get new Instance ID token
                                        deviceToken = task.getResult().getToken();


                                        // Log and toast


                                        usersRef.child(currentUserID).child("device_token").
                                                setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    SendUserToMainActivity();
                                                    Toast.makeText(RegisterActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                                    mLoadingBar.dismiss();

                                                }
                                            }
                                        });


                                    }
                                });

                                //String currentUserID = mAuth.getCurrentUser().getUid();
                                mDatabaseReference.child("Users").child(currentUserID).setValue("");

                                Toast.makeText(RegisterActivity.this,"Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                //send user to the main activity when a new account is created
                                SendUserToMainActivity();
                                //get user ID


                                mLoadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().toString();

                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                               mLoadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFileds() {
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtRegisterEmail = (EditText) findViewById(R.id.registerEmail);
        txtRegisterPassword = (EditText) findViewById(R.id.registerPassword);
        txtAlreadyHaveAnAccount= (TextView) findViewById(R.id.registerAlreadyHaveAnAccount);
        mLoadingBar = new ProgressDialog(RegisterActivity.this);
    }
    private void SendUserToLoginActivity() {
        Intent registerIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(registerIntent);

    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();

    }
}
