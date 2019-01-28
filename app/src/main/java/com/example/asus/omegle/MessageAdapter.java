package com.example.asus.omegle;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList){

        this.mMessageList = mMessageList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView messageText2;

        public MessageViewHolder(View view){
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            messageText2 = (TextView) view.findViewById(R.id.message_text_layout2);

        }

    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid().toString();

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom().toString();
        String message_type = c.getType();

        if(from_user.equals(current_user_id)){

            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageText2.setVisibility(View.VISIBLE);
            viewHolder.messageText2.setBackgroundResource(R.drawable.message_text_background2);
            viewHolder.messageText2.setTextColor(Color.WHITE);

            viewHolder.messageText2.setText(c.getMessage());


        }
        else{

            viewHolder.messageText2.setVisibility(View.GONE);
            viewHolder.messageText.setVisibility(View.VISIBLE);
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText.setTextColor(Color.WHITE);

            viewHolder.messageText.setText(c.getMessage());

        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
