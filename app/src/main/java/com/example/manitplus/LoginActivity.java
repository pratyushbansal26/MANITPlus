package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    EditText email,password;
    Button login,signup;
    TextView forgotpassword;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    String ScholarNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //OneSignal Initialization....

        OneSignal.initWithContext(this);
        OneSignal.setAppId(MainActivity.OneSignalAppId);


        email = findViewById(R.id.Email);
        password = findViewById(R.id.Password);
        login = findViewById(R.id.LogIn);
        signup = findViewById(R.id.SignUp);
        forgotpassword = findViewById(R.id.ForgotPassword);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().equals(""))
                    Toast.makeText(LoginActivity.this,"Please Enter The Email!",Toast.LENGTH_SHORT).show();
                else if(password.getText().toString().equals(""))
                    Toast.makeText(LoginActivity.this,"Please Enter Your Password!",Toast.LENGTH_SHORT).show();
                else
                {
                    //Creating ProgressDialog For Log In
                    final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setTitle("Log In");
                    progressDialog.setMessage("Logging In...");
                    progressDialog.show();

                    auth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                user = auth.getCurrentUser();

                                root.child("Colleges").child("MANIT").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        String scholarno = dataSnapshot.child("User Ids").child(user.getUid()).getValue().toString();
                                        DataSnapshot snapshot = dataSnapshot.child("Users").child(scholarno);

                                        //For Setting Player Id
                                        DatabaseReference userReference = root.child("Colleges").child("MANIT").child(snapshot.child("Year").getValue().toString()).
                                                child(snapshot.child("Branch").getValue().toString()).child(snapshot.child("Section").getValue().toString()).
                                                child("Player Ids").child(scholarno);

                                        //Waiting For Use Id To Be Assigned
                                        int i=0;
                                        while (OneSignal.getDeviceState().getUserId()==null)
                                        {
                                            i++;
                                            Log.i("yoyo2",""+i);
                                        }

                                        String playerId = OneSignal.getDeviceState().getUserId();

                                        //Setting User Id In Users And Section...
                                        root.child("Colleges").child("MANIT").child("Users").child(scholarno).child("Player Id").setValue(playerId);
                                        userReference.setValue(playerId);

                                        SharedPreferences sharedPreferences = getSharedPreferences("User Data", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();


                                        editor.putString("Scholar No", scholarno);
                                        editor.putString("User Id",auth.getCurrentUser().getUid());
                                        editor.putString("Email", auth.getCurrentUser().getEmail());
                                        editor.putString("Name", snapshot.child("Name").getValue().toString());
                                        editor.putString("Year", snapshot.child("Year").getValue().toString());
                                        editor.putString("Branch", snapshot.child("Branch").getValue().toString());
                                        editor.putString("Section", snapshot.child("Section").getValue().toString());
                                        editor.putString("Hostel", snapshot.child("Hostel").getValue().toString());
                                        editor.putString("Player Id", playerId);
                                        editor.putBoolean("Verified", false);
                                        editor.apply();


                                        progressDialog.dismiss();

                                        if (user.isEmailVerified())
                                        {
                                            Intent intent = new Intent(getApplicationContext(),SplashScreen.class);
                                            startActivity(intent);
                                            finish();

                                            Toast.makeText(LoginActivity.this, "Welcome Back!!", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                            Toast.makeText(LoginActivity.this, "Please Verify Your Email To Access The App Features...", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else
                                {
                                    progressDialog.dismiss();
                                    if(task.getException() instanceof FirebaseAuthInvalidUserException)
                                        Toast.makeText(LoginActivity.this,"User Does Not Exist!! Please Sign Up!!", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        //Password Reset
        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetmail = new EditText(getApplicationContext());

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Reset Password");
                builder.setMessage("Enter Your Email To Receive Password Reset Link");
                builder.setView(resetmail);
                builder.setNegativeButton("CANCEL",null);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String email = resetmail.getText().toString();
                        auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"A Link To Reset Password Has Been Sent To Your Email...",Toast.LENGTH_SHORT).show();
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Error!! Link Not Sent..",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.show();
            }
        });

        //SignUp
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
