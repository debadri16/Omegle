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

    private DatabaseReference mUserDatabase,mChatDatabase,mRootref;
    private String mCurrent_User_id, mChat_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mCurrent_User_id = mAuth.getCurrentUser().getUid();

        mChat_user_id = getIntent().getStringExtra("chat_user_id");

        mTxtView = (TextView)findViewById(R.id.start_textView);
        mTxtView2 = (TextView)findViewById(R.id.start_textView2);
        mCancelBtn = (Button)findViewById(R.id.start_cancel_btn);

        mTxtView.setText(mCurrent_User_id);
        mTxtView2.setText(mChat_user_id);

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("chat");

        //creating session
        if(! mChat_user_id.equals("waiting")){

            final HashMap<String, String> chat_userMap = new HashMap<>();
            chat_userMap.put("seen","false");

            mChatDatabase.child(mChat_user_id).child(mCurrent_User_id).setValue(chat_userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    mChatDatabase.child(mCurrent_User_id).child(mChat_user_id).setValue(chat_userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("creating session", "paired users");
                            Toast.makeText(StartActivity.this, "Session started",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

        }
        //searching for the other user
        else{
            mChatDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild(mCurrent_User_id)){

                        //correct formatting to get the key only
                        String temp = dataSnapshot.child(mCurrent_User_id).getValue().toString();
                        mChat_user_id = temp.substring(1,temp.indexOf("="));
                        mTxtView2.setText(mChat_user_id);

                        Log.d("creating session", "paired users");
                        Toast.makeText(StartActivity.this, "Session started",
                                Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startIntent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(startIntent);
                finish();

            }
        });

    }

    //little tweaks needed for locked state........pore dekha jabe
    @Override
    protected void onStop() {

        //deleting current user
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrent_User_id);
        mUserDatabase.removeValue();

        //clearing queue when single user
        if(mChat_user_id.equals("waiting")){

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("queue").child("userId");
            mUserDatabase.removeValue();

        }

        //deleting session
        mChatDatabase.child(mCurrent_User_id).removeValue();

        //sign-out current user
        FirebaseAuth.getInstance().signOut();

        mUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Log.d("Stopping", "Deleted user");

                        }
                    }
                });

        super.onStop();

    }

}
