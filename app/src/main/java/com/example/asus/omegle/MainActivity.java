package com.example.asus.omegle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button mChatBtn;
    private FirebaseAuth mAuth;
    private String mCurrent_User_id;
    private DatabaseReference mUserDatabase, mQueryDb;
    private Query mUserQuery;

    private ProgressDialog mChatProgress;

    private void createUserData(String chat_user_id, String status){
        mCurrent_User_id = mAuth.getCurrentUser().getUid();

        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("status", status);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrent_User_id);
        mUserDatabase.setValue(userMap);

        mChatProgress.dismiss();

        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startIntent.putExtra("chat_user_id", chat_user_id);
        startActivity(startIntent);
        finish();
    }

    private void queueUser(){
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("userId", mAuth.getCurrentUser().getUid());
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("queue");
        mUserDatabase.setValue(userMap);
    }
    private void updateUser(String Id, String status){
        HashMap<String, String> other_userMap = new HashMap<>();
        other_userMap.put("status", status);
        FirebaseDatabase.getInstance().getReference().child("users").child(Id).setValue(other_userMap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mChatBtn = (Button) findViewById(R.id.main_chat_btn);

        mChatProgress = new ProgressDialog(this);

        mChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mChatProgress.setTitle("Pairing In");
                mChatProgress.setMessage("Please wait while we're connecting you ...");
                mChatProgress.setCanceledOnTouchOutside(false);
                mChatProgress.show();

                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //authentication check
                        if(task.isSuccessful()){
                            //checking user queue
                            mQueryDb = FirebaseDatabase.getInstance().getReference().child("queue");
                            mQueryDb.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    long queueIsNotEmpty = dataSnapshot.getChildrenCount();
                                    //if queue is empty, create new user with status = available and queue the user
                                    if(queueIsNotEmpty == 0){
                                        queueUser();
                                        createUserData("waiting","available");
                                    }
                                    //if queue is not empty
                                    else{
                                        String other_userId = (String) dataSnapshot.child("userId").getValue();
                                        mQueryDb.removeValue();
                                        updateUser(other_userId,"busy");
                                        createUserData(other_userId,"busy");
//                                      createSession(otherUser) eta korte hbe;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                        else{
                            mChatProgress.dismiss();
                            Toast.makeText(MainActivity.this, "Chat didn't start",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }
}
