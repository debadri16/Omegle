package com.example.asus.omegle;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private TextView mTxtView;
    private TextView mTxtView2;
    private Button mCancelBtn;

    private DatabaseReference mQueueDb,mChatDatabase,mRootref;
    private String mCurrent_User_id, mChat_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mCurrent_User_id = mAuth.getCurrentUser().getUid();
        mChat_user_id = "waiting";

        mTxtView = (TextView)findViewById(R.id.start_textView);
        mTxtView2 = (TextView)findViewById(R.id.start_textView2);
        mCancelBtn = (Button)findViewById(R.id.start_cancel_btn);

        mTxtView.setText(mCurrent_User_id);
        mTxtView2.setText(mChat_user_id);

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("chat");

        mChatDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check if new chat session is created
                if(dataSnapshot.hasChild(mCurrent_User_id)){
                    if(mChat_user_id.equals("waiting")) {
                        //correct formatting to get the key only
                        String temp = dataSnapshot.child(mCurrent_User_id).getValue().toString();
                        mChat_user_id = temp.substring(1, temp.indexOf("="));
                        mTxtView2.setText(mChat_user_id);

                        Log.d("creating session", "paired users");
                        Toast.makeText(StartActivity.this, "Session started",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                //check if chat session is deleted
                else{
                    if(!mChat_user_id.equals("waiting")){
                        Toast.makeText(StartActivity.this, "Session ended",
                                Toast.LENGTH_SHORT).show();
                        mChat_user_id = "waiting";
                        mTxtView2.setText(mChat_user_id);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSessionUser();
                Intent startIntent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(startIntent);
                finish();
            }
        });

    }

    //little tweaks needed for locked state........pore dekha jabe
    @Override
    protected void onStop() {
        //deleteSessionUser();
        super.onStop();
    }

    private void deleteSessionUser(){
        //clearing queue when single user
        if(mChat_user_id.equals("waiting")){
            mQueueDb = FirebaseDatabase.getInstance().getReference().child("queue").child(mCurrent_User_id);
            mQueueDb.removeValue();
        }
        //deleting chat session
        else {
            mChatDatabase.child(mCurrent_User_id).removeValue();
            mChatDatabase.child(mChat_user_id).removeValue();
//            queueing other user
//            HashMap<String, Object> queueMap = new HashMap<>();
//            queueMap.put("timeStamp", ServerValue.TIMESTAMP);
//            mQueueDb = FirebaseDatabase.getInstance().getReference().child("queue").child(mChat_user_id);
//            mQueueDb.setValue(queueMap);
        }

        //sign-out current user
        FirebaseAuth.getInstance().signOut();

        //deleting current user
        mUser.delete()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("Stopping", "Deleted user");
                }
                }
            });
    }

}
