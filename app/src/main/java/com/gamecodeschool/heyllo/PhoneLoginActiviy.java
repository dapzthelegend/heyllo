package com.gamecodeschool.heyllo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.RecoverySystem;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActiviy extends AppCompatActivity {
    private Button btnVerify, btnSendVerificationCode;
    private EditText txtPhoneNumber, txtVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth ;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login_activiy);
        mAuth = FirebaseAuth.getInstance();
        InitializeFields();
        //after the fields are initialize set a on click listener for
        //btnSendVerificationCode such that when clicked,
        mProgressDialog = new ProgressDialog(PhoneLoginActiviy.this);
        btnSendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendVerificationCode.setVisibility(View.INVISIBLE);
                txtPhoneNumber.setVisibility(View.INVISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
                txtVerificationCode.setVisibility(View.VISIBLE);

                //get the phone number from the txtPhoneNumber
                String phoneNumber = txtPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActiviy.this, "Please  enter a number", Toast.LENGTH_SHORT ).show();
                }

                else {
                    mProgressDialog.setTitle("Phone number verification");
                    mProgressDialog.setMessage("please wait while verification number is sent");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActiviy.this,               // Activity (for callback binding)
                            // OnVerificationStateChangedCallbacks
                            callbacks);
                }


            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendVerificationCode.setVisibility(View.INVISIBLE);
                txtPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = txtVerificationCode.getText().toString();

                if(TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActiviy.this, "please enter verification code", Toast.LENGTH_SHORT).show();
                }

                else{
                    mProgressDialog.setTitle("verification of code");
                    mProgressDialog.setMessage("please wait while we are veryfying code");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }

            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                 signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mProgressDialog.dismiss();
                 Toast.makeText(PhoneLoginActiviy.this, "invalid verification code", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                mProgressDialog.dismiss();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;


            }
        };
        }


    private void InitializeFields() {
        btnSendVerificationCode = (Button) findViewById(R.id.btnSendVerificationCode);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        txtPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        txtVerificationCode = (EditText) findViewById(R.id.phoneVerificationCode);



    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();

                            Toast.makeText(PhoneLoginActiviy.this, "Log in successful", Toast.LENGTH_SHORT).show();
                            SendUserToMainActvity();

                        } else {
                            mProgressDialog.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActiviy.this,message, Toast.LENGTH_SHORT).show();
                            //
                        }
                    }
                });
    }

    private void SendUserToMainActvity() {
        Intent mainIntent = new Intent(PhoneLoginActiviy.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
