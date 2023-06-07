package com.example.benschat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private ChatAdapter chatAdapter;
  private EditText editTextMessage;
  private Button buttonSend;

  private DatabaseReference messagesRef;
  private ChildEventListener messagesListener;

  private static final int RC_SIGN_IN = 100;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    // Initialize Firebase
    messagesRef = FirebaseDatabase.getInstance().getReference("messages");

    // Set up RecyclerView
    recyclerView = findViewById(R.id.recyclerViewChat);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    chatAdapter = new ChatAdapter(this, new ArrayList<>(), getCurrentUserId(), new ArrayList<>());
    recyclerView.setAdapter(chatAdapter);


    // Set up EditText and Button
    editTextMessage = findViewById(R.id.editTextMessage);
    buttonSend = findViewById(R.id.buttonSend);
    buttonSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendMessage();
      }
    });

    // Check if the user is already signed in
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    if (account != null) {
      // User is signed in, retrieve messages
      retrieveMessages();
    } else {
      // User is not signed in, start the sign-in flow
      startSignIn();
    }
  }

  private String getCurrentUserId() {
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    if (account != null) {
      return account.getId();
    }
    return null;
  }

  private void retrieveMessages() {
    messagesListener = messagesRef.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        ChatMessage message = snapshot.getValue(ChatMessage.class);
        chatAdapter.addMessage(message);
        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {
      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Toast.makeText(ChatActivity.this, "Error retrieving messages", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void sendMessage() {
    String messageText = editTextMessage.getText().toString().trim();
    if (!TextUtils.isEmpty(messageText)) {
      String messageId = messagesRef.push().getKey();
      long timestamp = System.currentTimeMillis();
      ChatMessage message = new ChatMessage(messageId, getCurrentUserId(), "", messageText, timestamp);
      messagesRef.child(messageId).setValue(message);
      editTextMessage.setText("");
    }
  }

  private void startSignIn() {
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build();
    GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);
    Intent signInIntent = signInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        // Signed in successfully, retrieve messages
        retrieveMessages();
      } catch (ApiException e) {
        Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (messagesListener != null) {
      messagesRef.removeEventListener(messagesListener);
    }
  }
}
