package com.example.benschat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    SignInButton signInButton;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth firebaseAuth;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleGoogleSignIn(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        if (account != null) {
            // Obtain the necessary user information
            String displayName = account.getDisplayName();
            String email = account.getEmail();
            String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

            if (email != null && !email.isEmpty()) {
                // Check if the user already exists in the Firebase Realtime Database
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User already exists
                            Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                            // Proceed with your app's logic
                            // For example, start the ChatActivity
                            startActivity(new Intent(MainActivity.this, ChatActivity.class));
                            finish();
                        } else {
                            // User does not exist, create a new user in the Firebase Realtime Database
                            String userId = usersRef.push().getKey();

                            User user = new User(userId, displayName, email);

                            if (photoUrl != null) {
                                user.setPhotoUrl(photoUrl);
                            }

                            usersRef.child(userId).setValue(user)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // User creation successful
                                            Toast.makeText(MainActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                            // Proceed with your app's logic
                                            // For example, start the ChatActivity
                                            startActivity(new Intent(MainActivity.this, ChatActivity.class));
                                            finish();
                                        } else {
                                            // User creation failed
                                            Toast.makeText(MainActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle the error
                        Toast.makeText(MainActivity.this, "Error retrieving user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Email is null or empty
                Toast.makeText(MainActivity.this, "Failed to retrieve email", Toast.LENGTH_SHORT).show();
            }
        } else {
            // GoogleSignInAccount is null
            Toast.makeText(MainActivity.this, "Failed to retrieve Google account", Toast.LENGTH_SHORT).show();
        }
    }
}
