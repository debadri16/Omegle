package com.example.asus.omegle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private Button mChatBtn;
    private FirebaseAuth mAuth;
    private String mCurrent_User_id;
    private DatabaseReference mUserDatabase;
    private Query mUserQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mChatBtn = (Button) findViewById(R.id.main_chat_btn);

        mChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //authentication check
                        if(task.isSuccessful()){
                            //check for the latest available user
                            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                            mUserQuery = mUserDatabase.orderByKey().limitToFirst(1);

                            mUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String status = dataSnapshot.child("status").getValue().toString();
//                                    Toast.makeText(MainActivity.this, "status" ,
//                                            Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Handle possible errors.
                                }
                            });


//                            mCurrent_User_id = mAuth.getCurrentUser().getUid();
//
//                            HashMap<String, String> userMap = new HashMap<>();
//                            userMap.put("status", "available");
//
//                            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrent_User_id);
//                            mUserDatabase.setValue(userMap);
//
//                            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
//                            startActivity(startIntent);
//                            finish();

                        }
                        else{
                            Toast.makeText(MainActivity.this, "Chat didn't start",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }
}
