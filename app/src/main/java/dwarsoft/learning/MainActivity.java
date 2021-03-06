package dwarsoft.learning;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    Button btEnter,btTeacher;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String uid,name,mail;
    int type = 3;
    int i = 0;

    ProgressBar mainprogress;
    //type 1 for student, type 2 for teachers
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd("background.mp3");
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mainprogress = findViewById(R.id.mainprogress);
        mainprogress.setVisibility(View.GONE);
        btEnter = findViewById(R.id.btEnter);
        btTeacher = findViewById(R.id.btTeacher);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        
        btTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable())
                {
                    type = 2;
                    signIn();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable())
                {

                    mainprogress.setVisibility(View.VISIBLE);
                    type = 1;
                    signIn();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signIn() {
        FirebaseAuth.getInstance().signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                mainprogress.setVisibility(View.GONE);
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser user = mAuth.getCurrentUser();
                    uid = user.getUid();
                    mail = acct.getEmail();
                    name = acct.getDisplayName();
                    
                    if (type==1)
                    {
                        DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference();
                        databaseref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("profile/"+uid))
                                {
                                    mainprogress.setVisibility(View.GONE);
                                    Intent intent = new Intent (MainActivity.this, HomeScreen.class);
                                    startActivity(intent);
                                }
                                else
                                {
                                    mainprogress.setVisibility(View.GONE);
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference databaseReference = database.getReference();
                                    databaseReference.child("profile/"+uid+"/name").setValue(name);
                                    databaseReference.child("profile/"+uid+"/mail").setValue(mail);
                                    databaseReference.child("profile/"+uid+"/points").setValue("0");
                                    Intent intent = new Intent (MainActivity.this, HomeScreen.class);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else
                {
                    mainprogress.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Sign in failed...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
