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

public class StartActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private TextView mTxtView;
    private Button mCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mTxtView = (TextView)findViewById(R.id.start_textView);
        mCancelBtn = (Button)findViewById(R.id.start_cancel_btn);

        mTxtView.setText(mAuth.getCurrentUser().getUid());

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();

                Intent startIntent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(startIntent);
                finish();

            }
        });

    }

    @Override
    protected void onStop() {

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
