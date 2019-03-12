package com.gamecodeschool.heyllo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin, btnPhoneLogin;
    private EditText txtEmail, txtPassword;
    private TextView txtNeedNewAccount, txtForgetPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoadingBar;
    private DatabaseReference usersRef;
    private String deviceToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //call a method to initialize the Fields
        InitializeFields();

        //Authorization to login
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        txtNeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        btnPhoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPhoneLoginActivity();
            }
        });
    }

    private void AllowUserToLogin() {
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {

            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
        } else {

            //display the loading bar
            mLoadingBar.setTitle("Sign in");
            mLoadingBar.setMessage("Please wait ");
            mLoadingBar.setCanceledOnTouchOutside(true);
            mLoadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

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
                                                    Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                                    mLoadingBar.dismiss();

                                                }
                                            }
                                        });


                                    }
                                });





                            }
                            else{
                                String message = task.getException().toString();

                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                mLoadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnPhoneLogin = (Button) findViewById(R.id.btnPhoneLogin);
        txtEmail = (EditText) findViewById(R.id.loginEmail);
        txtPassword = (EditText) findViewById(R.id.loginPassword);
        txtForgetPassword = (TextView) findViewById(R.id.forgetPassword);
        txtNeedNewAccount = (TextView) findViewById(R.id.needNewAccount);
        mLoadingBar = new ProgressDialog(LoginActivity.this);
    }



    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);
        finish();

    }
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);

    }


    private void SendUserToPhoneLoginActivity() {
        Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActiviy.class);
        phoneLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(phoneLoginIntent);
        finish();

    }
}
