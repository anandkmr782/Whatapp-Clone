package com.example.whatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.whatapp.Adapter.ChatAdapter;
import com.example.whatapp.Model.MessageModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatsActivity extends AppCompatActivity {
    //ActivityChatsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    TextView tvProfileName;
    EditText etMessage;
    ImageView imageSend;
    RecyclerView recyclerViewChats;
    ImageView imageArrow,image,imagecall,imageVideoCall,imageMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);
       getSupportActionBar().hide();

        tvProfileName=findViewById(R.id.tvProfileName);
        imageArrow=findViewById(R.id.imageArrow);
        image=findViewById(R.id.image);
        imagecall=findViewById(R.id.imagecall);
        imageVideoCall=findViewById(R.id.imageVideoCall);
        etMessage=findViewById(R.id.etMessage);
        imageSend=findViewById(R.id.imageSend);
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

       final String senderId=auth.getUid();
        String recieveId=getIntent().getStringExtra("userId");
        String userName=getIntent().getStringExtra("userName");
        String profilePic=getIntent().getStringExtra("profilePic");
        tvProfileName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.profile).into(image);

        imageArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels,this);
        recyclerViewChats.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerViewChats.setLayoutManager(layoutManager);


        final String senderRoom=senderId +recieveId;
        final String recieverRoom=recieveId + senderId;
        database.getReference().child("chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for(DataSnapshot snapshot1:snapshot.getChildren())
                        {
                            MessageModel models=snapshot1.getValue(MessageModel.class);
                            messageModels.add(models);
                        }
                        chatAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=etMessage.getText().toString();
                final MessageModel model=new MessageModel(senderId,message);
                model.setTimestamp(new Date().getTime());
                etMessage.setText("");
                database.getReference().child("chats").child(senderRoom).push().setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats").child(recieverRoom).push()
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                    }
                                });

                            }
                        });
            }
        });
    }
}