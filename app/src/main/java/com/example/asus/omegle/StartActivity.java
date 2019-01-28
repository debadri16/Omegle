package com.example.asus.omegle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    //ager baaler jinish....gone visibility
    private TextView mTxtView;
    private TextView mTxtView2;
    private TextView mTxtView3;
    private Button mCancelBtn;

    //new stuffs
    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessageList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 12;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    private DatabaseReference mQueueDb,mChatDatabase,mRootRef;
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
        mTxtView3 = (TextView)findViewById(R.id.waiting_textView);

        mCancelBtn = (Button)findViewById(R.id.start_cancel_btn);

        mTxtView.setText(mCurrent_User_id);
        mTxtView2.setText(mChat_user_id);

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("chat");
        mRootRef = FirebaseDatabase.getInstance().getReference();


        //new rcycler views and all
        mChatAddBtn = (ImageButton)findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton)findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_msgView);

        mAdapter = new MessageAdapter(messagesList);

        mMessageList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);

        mMessageList.setAdapter(mAdapter);




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

                        //start child listener
                        mChatAddBtn.setVisibility(View.VISIBLE);
                        mChatSendBtn.setVisibility(View.VISIBLE);
                        mChatMessageView.setVisibility(View.VISIBLE);
                        mTxtView3.setVisibility(View.GONE);
                        loadMessages();

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
                        mTxtView3.setVisibility(View.VISIBLE);
                        mTxtView3.setText("Valar Morghulis");
                        mChatAddBtn.setVisibility(View.INVISIBLE);
                        mChatSendBtn.setVisibility(View.INVISIBLE);
                        mChatMessageView.setVisibility(View.INVISIBLE);
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

        //send and refresh
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });


    }

    //little tweaks needed for locked state........pore dekha jabe
    @Override
    protected void onStop() {
        deleteSessionUser();
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
            mRootRef.child("messages").child(mCurrent_User_id).removeValue();
            mRootRef.child("messages").child(mChat_user_id).removeValue();
//            queueing other user, not needed rigth now
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


    //kaajer jinish muchbi na
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrent_User_id).child(mChat_user_id);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, message);
                }
                else{
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1){

                    mLastKey = messageKey;
                }

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(12,0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrent_User_id).child(mChat_user_id);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;
                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String currentUserRef = "messages/" + mCurrent_User_id + "/" + mChat_user_id;
            String chatUserRef = "messages/" + mChat_user_id + "/" + mCurrent_User_id;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrent_User_id).child(mChat_user_id).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrent_User_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + push_id, messageMap);
            messageUserMap.put(chatUserRef + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }

    }

}
