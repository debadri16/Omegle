package com.example.asus.omegle;

import android.app.ProgressDialog;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button mChatBtn;
    private FirebaseAuth mAuth;
    private String mCurrent_User_id;
    private DatabaseReference mQueueDb;
    private ProgressDialog mChatProgress;

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
            mChatProgress.setMessage("Please wait while we are connecting you.");
            mChatProgress.setCanceledOnTouchOutside(false);
            mChatProgress.show();

            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //authentication check
                    if(task.isSuccessful()){
                        //queueing user
                        mCurrent_User_id = mAuth.getCurrentUser().getUid();
                        mQueueDb = FirebaseDatabase.getInstance().getReference().child("queue").child(mCurrent_User_id);
                        HashMap<String, Object> queueMap = new HashMap<>();
                        queueMap.put("timeStamp", ServerValue.TIMESTAMP);
                        mQueueDb.setValue(queueMap);

                        mChatProgress.dismiss();
                        //starting new activity
                        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(startIntent);
                        finish();
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
