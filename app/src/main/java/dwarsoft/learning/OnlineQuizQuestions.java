package dwarsoft.learning;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static dwarsoft.learning.QuizLoading.CATPREF;

public class OnlineQuizQuestions extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String uid;
    private SharedPreferences categoriesPref;
    private SharedPreferences.Editor editor;

    Button btOnlineProceed;

    TextView tvOnlineOwn,tvOnlineOpposite;
    ProgressBar pbOnlineOpposite,pbOnlineOwn;

    String ownpoints,ownname,oppname,opppoints,oppuid;

    int nextpossible;

    TextView tvOnlineQuizQuestions;
    RadioGroup rgOnlineQuizOptions;
    RadioButton rbOnlineQuizOption1,rbOnlineQuizOption2,rbOnlineQuizOption3,rbOnlineQuizOption4;
    Button btOnlineQuiz;

    TextView tvOnlineRemaining;
    Button btOnlineoption1,btOnlineoption2,btOnlineoption3,btOnlineoption4;

    int size,correct,wrong,answered,last = 0;
    String correctanswer,explanation;

    int yes = 0,oppquesno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_quiz_questions);

        tvOnlineRemaining = findViewById(R.id.tvOnlineRemaining);

        btOnlineoption1 = findViewById(R.id.btOnlineoption1);
        btOnlineoption2 = findViewById(R.id.btOnlineoption2);
        btOnlineoption3 = findViewById(R.id.btOnlineoption3);
        btOnlineoption4 = findViewById(R.id.btOnlineoption4);

        tvOnlineQuizQuestions = findViewById(R.id.tvOnlineQuizQuestions);
        btOnlineProceed = findViewById(R.id.btOnlineProceed);

        SharedPreferences catPref = getSharedPreferences(CATPREF, Context.MODE_PRIVATE);
        correct = catPref.getInt("onlinecorrect", 0);
        wrong = catPref.getInt("onlinewrong", 0);
        answered = catPref.getInt("onlineanswered", 0);
        size = catPref.getInt("onlinecount", 0);

        tvOnlineRemaining.setText("Question "+String.valueOf(answered)+"/"+String.valueOf(size));

        nextpossible = answered + 1;
        if (answered<size)
        {
            //fetch from sharedpref. set to display
            tvOnlineQuizQuestions.setText(catPref.getString("onlinequestion"+answered,null));
            btOnlineoption1.setText(catPref.getString("onlineoption1"+answered,null));
            btOnlineoption2.setText(catPref.getString("onlineoption2"+answered,null));
            btOnlineoption3.setText(catPref.getString("onlineoption3"+answered,null));
            btOnlineoption4.setText(catPref.getString("onlineoption4"+answered,null));
            correctanswer = catPref.getString("onlinecorrect"+answered,null);
            explanation = catPref.getString("onlineexplanation"+answered,null);

        }
        if (answered==size-1)
        {
            last = 1;
        }


        tvOnlineOwn = findViewById(R.id.tvOnlineOwn);
        tvOnlineOpposite = findViewById(R.id.tvOnlineOpposite);
        pbOnlineOpposite = findViewById(R.id.pbOnlineOpposite);
        pbOnlineOwn = findViewById(R.id.pbOnlineOwn);

        categoriesPref = getSharedPreferences(CATPREF, Context.MODE_PRIVATE);
        oppuid = categoriesPref.getString("onlineuid",null);
        oppname = categoriesPref.getString("onlinename",null);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                    ownname = user.getDisplayName();

                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference();
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            DatabaseReference usersReff = FirebaseDatabase.getInstance().getReference();
                            usersReff.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    ownpoints = dataSnapshot.child("playing/"+uid+"/points").getValue().toString();
                                    opppoints = dataSnapshot.child("playing/"+oppuid+"/points").getValue().toString();

                                    int ownp = Integer.parseInt(ownpoints);
                                    int oppp = Integer.parseInt(opppoints);
                                    tvOnlineOwn.setText(ownname);
                                    tvOnlineOpposite.setText(oppname);
                                    pbOnlineOwn.setProgress(ownp);
                                    pbOnlineOpposite.setProgress(oppp);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            btOnlineoption1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (yes==0)
                                    {

                                        btOnlineoption1.setOnClickListener(null);
                                        btOnlineoption2.setOnClickListener(null);
                                        btOnlineoption3.setOnClickListener(null);
                                        btOnlineoption4.setOnClickListener(null);

                                        String selectedtext = btOnlineoption1.getText().toString();

                                        categoriesPref = getSharedPreferences(CATPREF, Context.MODE_PRIVATE);
                                        editor = categoriesPref.edit();
                                        answered = answered + 1;
                                        editor.putInt("onlineanswered",answered);
                                        editor.commit();

                                        String a = String.valueOf(answered);

                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference databaseReference = database.getReference();
                                        databaseReference.child("playing/"+uid+"/question").setValue(a);
                                        //set in db also

                                        btOnlineProceed.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                check();
                                            }
                                        });

                                        if (selectedtext.equals(correctanswer))
                                        {

                                            //credit points to the user
                                            correct = correct + 1;
                                            editor.putInt("onlinecorrect",correct);
                                            editor.commit();

                                            int update = Integer.parseInt(ownpoints);
                                            update = update + 20;
                                            String newupdate = String.valueOf(update);
                                            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                            DatabaseReference databaseReference1 = database1.getReference();
                                            databaseReference1.child("playing/"+uid+"/points").setValue(newupdate);

                                            new AlertDialog.Builder(OnlineQuizQuestions.this)
                                                    .setMessage("Correct answer! "+explanation+" .")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Next", null)
                                                    .show();
                                        }
                                        else
                                        {
                                            wrong = wrong + 1;
                                            editor.putInt("onlinewrong",wrong);
                                            editor.commit();

                                            new AlertDialog.Builder(OnlineQuizQuestions.this)
                                                    .setMessage("Wrong answer! "+explanation+" .")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Next",null)
                                                    .show();
                                        }
                                        if (last==0)
                                        {

                                        }
                                        else
                                        {
                                            new AlertDialog.Builder(OnlineQuizQuestions.this)
                                                    .setMessage("You have finished the quiz. Results: Right Answers: "+String.valueOf(correct)+", Wrong answer: "+String.valueOf(wrong))
                                                    .setCancelable(false)
                                                    .setPositiveButton("FINISH", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Toast.makeText(OnlineQuizQuestions.this, "Finished", Toast.LENGTH_SHORT).show();

                                                            Intent intent = new Intent (OnlineQuizQuestions.this, ResultPage.class);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .show();
                                        }
                                        yes = 1;
                                    }
                                    else
                                    {
                                        Toast.makeText(OnlineQuizQuestions.this, "Wait for opp...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                            btOnlineoption2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (yes==0)
                                    {
                                        btOnlineoption1.setOnClickListener(null);
                                        btOnlineoption2.setOnClickListener(null);
                                        btOnlineoption3.setOnClickListener(null);
                                        btOnlineoption4.setOnClickListener(null);
                                        String selectedtext = btOnlineoption2.getText().toString();

                                        categoriesPref = getSharedPreferences(CATPREF, Context.MODE_PRIVATE);
                                        editor = categoriesPref.edit();
                                        answered = answered + 1;
                                        editor.putInt("onlineanswered",answered);
                                        editor.commit();

                                        String a = String.valueOf(answered);

                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference databaseReference = database.getReference();
                                        databaseReference.child("playing/"+uid+"/question").setValue(a);
                                        //set in db also

                                        btOnlineProceed.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                check();
                                            }
                                        });

                                        if (selectedtext.equals(correctanswer))
                                        {

                                            //credit points to the user
                                            correct = correct + 1;
                                            editor.putInt("onlinecorrect",correct);
                                            editor.commit();

                                            int update = Integer.parseInt(ownpoints);
                                            update = update + 20;
                                            String newupdate = String.valueOf(update);
                                            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                            DatabaseReference databaseReference1 = database1.getReference();
                                            databaseReference1.child("playing/"+uid+"/points").setValue(newupdate);

                                            new AlertDialog.Builder(OnlineQuizQuestions.this)
                                                    .setMessage("Correct answer! "+explanation+" .")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Next", null)
                                                    .show();
                                        }
                                        else
                                        {
                                            wrong = wrong + 1;
                                            editor.putInt("onlinewrong",wrong);
                                            editor.commit();

                                            new AlertDialog.Builder(OnlineQuizQuestions.this)
                                                    .setMessage("Wrong answer! "+explanation+" .")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Next",null)
                                                    .show();
                                        }
                                        if (last==0)
                                        {

                                        }
                                        else
                                        {
                                            new AlertDialog.Builder(OnlineQuizQuestions.this)
                                                    .setMessage("You have finished the quiz. Results: Right Answers: "+String.valueOf(correct)+", Wrong answer: "+String.valueOf(wrong))
                                                    .setCancelable(false)
                                                    .setPositiveButton("FINISH", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Toast.makeText(OnlineQuizQuestions.this, "Finished", Toast.LENGTH_SHORT).show();

                                                            Intent intent = new Intent (OnlineQuizQuestions.this, ResultPage.class);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .show();
                                        }
                                        yes = 1;
                                    }
                                    else
                                    {
                                        Toast.makeText(OnlineQuizQuestions.this, "Wait for opp...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    btOnlineoption3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (yes==0)
                            {
                                btOnlineoption1.setOnClickListener(null);
                                btOnlineoption2.setOnClickListener(null);
                                btOnlineoption3.setOnClickListener(null);
                                btOnlineoption4.setOnClickListener(null);
                                String selectedtext = btOnlineoption3.getText().toString();

                                categoriesPref = getSharedPreferences(CATPREF, Context.MODE_PRIVATE);
                                editor = categoriesPref.edit();
                                answered = answered + 1;
                                editor.putInt("onlineanswered",answered);
                                editor.commit();

                                String a = String.valueOf(answered);

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = database.getReference();
                                databaseReference.child("playing/"+uid+"/question").setValue(a);
                                //set in db also

                                btOnlineProceed.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        check();
                                    }
                                });

                                if (selectedtext.equals(correctanswer))
                                {

                                    //credit points to the user
                                    correct = correct + 1;
                                    editor.putInt("onlinecorrect",correct);
                                    editor.commit();

                                    int update = Integer.parseInt(ownpoints);
                                    update = update + 20;
                                    String newupdate = String.valueOf(update);
                                    FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                    DatabaseReference databaseReference1 = database1.getReference();
                                    databaseReference1.child("playing/"+uid+"/points").setValue(newupdate);

                                    new AlertDialog.Builder(OnlineQuizQuestions.this)
                                            .setMessage("Correct answer! "+explanation+" .")
                                            .setCancelable(false)
                                            .setPositiveButton("Next", null)
                                            .show();
                                }
                                else
                                {
                                    wrong = wrong + 1;
                                    editor.putInt("onlinewrong",wrong);
                                    editor.commit();

                                    new AlertDialog.Builder(OnlineQuizQuestions.this)
                                            .setMessage("Wrong answer! "+explanation+" .")
                                            .setCancelable(false)
                                            .setPositiveButton("Next",null)
                                            .show();
                                }
                                if (last==0)
                                {

                                }
                                else
                                {
                                    new AlertDialog.Builder(OnlineQuizQuestions.this)
                                            .setMessage("You have finished the quiz. Results: Right Answers: "+String.valueOf(correct)+", Wrong answer: "+String.valueOf(wrong))
                                            .setCancelable(false)
                                            .setPositiveButton("FINISH", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Toast.makeText(OnlineQuizQuestions.this, "Finished", Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent (OnlineQuizQuestions.this, ResultPage.class);
                                                    startActivity(intent);
                                                }
                                            })
                                            .show();
                                }
                                yes = 1;
                            }
                            else
                            {
                                Toast.makeText(OnlineQuizQuestions.this, "Wait for opp...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btOnlineoption4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (yes==0)
                            {
                                btOnlineoption1.setOnClickListener(null);
                                btOnlineoption2.setOnClickListener(null);
                                btOnlineoption3.setOnClickListener(null);
                                btOnlineoption4.setOnClickListener(null);
                                String selectedtext = btOnlineoption4.getText().toString();

                                categoriesPref = getSharedPreferences(CATPREF, Context.MODE_PRIVATE);
                                editor = categoriesPref.edit();
                                answered = answered + 1;
                                editor.putInt("onlineanswered",answered);
                                editor.commit();

                                String a = String.valueOf(answered);

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = database.getReference();
                                databaseReference.child("playing/"+uid+"/question").setValue(a);
                                //set in db also

                                btOnlineProceed.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        check();
                                    }
                                });

                                if (selectedtext.equals(correctanswer))
                                {

                                    //credit points to the user
                                    correct = correct + 1;
                                    editor.putInt("onlinecorrect",correct);
                                    editor.commit();

                                    int update = Integer.parseInt(ownpoints);
                                    update = update + 20;
                                    String newupdate = String.valueOf(update);
                                    FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                                    DatabaseReference databaseReference1 = database1.getReference();
                                    databaseReference1.child("playing/"+uid+"/points").setValue(newupdate);

                                    new AlertDialog.Builder(OnlineQuizQuestions.this)
                                            .setMessage("Correct answer! "+explanation+" .")
                                            .setCancelable(false)
                                            .setPositiveButton("Next", null)
                                            .show();
                                }
                                else
                                {
                                    wrong = wrong + 1;
                                    editor.putInt("onlinewrong",wrong);
                                    editor.commit();

                                    new AlertDialog.Builder(OnlineQuizQuestions.this)
                                            .setMessage("Wrong answer! "+explanation+" .")
                                            .setCancelable(false)
                                            .setPositiveButton("Next",null)
                                            .show();
                                }
                                if (last==0)
                                {

                                }
                                else
                                {
                                    new AlertDialog.Builder(OnlineQuizQuestions.this)
                                            .setMessage("You have finished the quiz. Results: Right Answers: "+String.valueOf(correct)+", Wrong answer: "+String.valueOf(wrong))
                                            .setCancelable(false)
                                            .setPositiveButton("FINISH", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Toast.makeText(OnlineQuizQuestions.this, "Finished", Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent (OnlineQuizQuestions.this, ResultPage.class);
                                                    startActivity(intent);
                                                }
                                            })
                                            .show();
                                }
                                yes = 1;
                            }
                            else
                            {
                                Toast.makeText(OnlineQuizQuestions.this, "Wait for opp...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(OnlineQuizQuestions.this, "Session lost...", Toast.LENGTH_SHORT).show();
                }
            }
        };
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

    public void check()
    {
        DatabaseReference usersRefff = FirebaseDatabase.getInstance().getReference("playing");
        usersRefff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        String oppquestion = dataSnapshot.child(oppuid+"/question").getValue().toString();
                        int opppp = Integer.parseInt(oppquestion);
                        if (nextpossible==answered)
                        {
                            if (yes==1)
                            {
                                if (answered==opppp)
                                {

                                    //both can proceed
                                    finish();
                                    startActivity(getIntent());
                                }
                                else if (answered<opppp)
                                {
                                    Toast.makeText(OnlineQuizQuestions.this, "Opp is waiting...", Toast.LENGTH_SHORT).show();
                                }
                                else if (answered>opppp)
                                {
                                    Toast.makeText(OnlineQuizQuestions.this, "Please wait for opp...", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(OnlineQuizQuestions.this, "Please answer the question...", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(OnlineQuizQuestions.this, "Please answer...", Toast.LENGTH_SHORT).show();
                        }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
