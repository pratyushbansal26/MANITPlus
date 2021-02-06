package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity {

    EditText scholarno,password,confirmpassword,email;
    Button signup,login;
    Spinner hostel;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.Email);
        scholarno = findViewById(R.id.ScholarNo);
        password = findViewById(R.id.Password);
        confirmpassword = findViewById(R.id.ConfirmPassword);
        signup = findViewById(R.id.SignUp);
        hostel = findViewById(R.id.Hostel);
        login = findViewById(R.id.LogInButton);

        //OneSignal Initialization....

        OneSignal.initWithContext(this);
        OneSignal.setAppId(MainActivity.OneSignalAppId);



        //Fill Hostel Spinner
        ArrayList<String> SpinnerList = new ArrayList<String>(Arrays.asList("Choose","H-1","H-2","H-3","H-4","H-5","H-6","H-7","H-8","H-9","H-10","Day Scholar"));
        ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<>(getApplication(),android.R.layout.simple_spinner_dropdown_item,SpinnerList);
        hostel.setAdapter(SpinnerAdapter);


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(email.getText().toString().equals(""))
                    Toast.makeText(SignUpActivity.this,"Please Enter Your Email!",Toast.LENGTH_SHORT).show();
                else if(scholarno.getText().toString().equals(""))
                    Toast.makeText(SignUpActivity.this,"Please Enter Your Scholar Number!",Toast.LENGTH_SHORT).show();
                else if(password.getText().toString().equals(""))
                    Toast.makeText(SignUpActivity.this,"Password Cannot Be Empty!",Toast.LENGTH_SHORT).show();
                else if(!password.getText().toString().equals(confirmpassword.getText().toString()))
                    Toast.makeText(SignUpActivity.this,"Password And Confirm Password Do Not Match!",Toast.LENGTH_SHORT).show();
                else if(hostel.getSelectedItem().toString().equals("Choose"))
                    Toast.makeText(SignUpActivity.this,"Please Select Your Hostel!",Toast.LENGTH_SHORT).show();
                else
                {

                    //Creating ProgressDialog For Sign Up
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setTitle("Sign Up");
                    progressDialog.setMessage("Creating Account...");
                    progressDialog.show();

                    root.child("Colleges").child("MANIT").child("Verification").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(scholarno.getText().toString()).exists())
                            {
                                if (dataSnapshot.child(scholarno.getText().toString()).child("Email").getValue().toString().equals(email.getText().toString()))
                                {

                                    final DataSnapshot snapshot = dataSnapshot.child(scholarno.getText().toString());

                                    auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {

                                                    if (task.isSuccessful()) {

                                                        root.child("Colleges").child("MANIT").child("User Ids").child(auth.getCurrentUser().getUid().toString()).setValue(scholarno.getText().toString());
                                                        final DatabaseReference user = root.child("Colleges").child("MANIT").child("Users").child(scholarno.getText().toString());

                                                        //Creating User Node In Users & Shared Preferences

                                                        user.child("Scholar No").setValue(snapshot.getKey());
                                                        user.child("User Id").setValue(auth.getCurrentUser().getUid());
                                                        user.child("Email").setValue(snapshot.child("Email").getValue().toString());
                                                        user.child("Name").setValue(snapshot.child("Name").getValue().toString());
                                                        user.child("Year").setValue(snapshot.child("Year").getValue().toString());
                                                        user.child("Branch").setValue(snapshot.child("Branch").getValue().toString());
                                                        user.child("Section").setValue(snapshot.child("Section").getValue().toString());
                                                        user.child("Hostel").setValue(hostel.getSelectedItem().toString());

                                                        //For Setting Player Id
                                                        DatabaseReference userReference = root.child("Colleges").child("MANIT").child(snapshot.child("Year").getValue().toString()).
                                                                child(snapshot.child("Branch").getValue().toString()).child(snapshot.child("Section").getValue().toString()).
                                                                child("Player Ids").child(snapshot.getKey());

                                                        //Waiting For Use Id To Be Assigned
                                                        int i=0;
                                                        while (OneSignal.getDeviceState().getUserId()==null)
                                                        {
                                                            i++;
                                                            Log.i("yoyo2",""+i);
                                                        }

                                                        String playerId = OneSignal.getDeviceState().getUserId();

                                                        //Saving User Id In Users And Section
                                                        root.child("Colleges").child("MANIT").child("Users").child(scholarno.getText().toString()).child("Player Id").setValue(playerId);
                                                        userReference.setValue(playerId);


                                                        SharedPreferences sharedPreferences = getSharedPreferences("User Data", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();


                                                        editor.putString("Scholar No", scholarno.getText().toString());
                                                        editor.putString("User Id",auth.getCurrentUser().getUid());
                                                        editor.putString("Email", auth.getCurrentUser().getEmail());
                                                        editor.putString("Name", snapshot.child("Name").getValue().toString());
                                                        editor.putString("Year", snapshot.child("Year").getValue().toString());
                                                        editor.putString("Branch", snapshot.child("Branch").getValue().toString());
                                                        editor.putString("Section", snapshot.child("Section").getValue().toString());
                                                        editor.putString("Hostel", hostel.getSelectedItem().toString());
                                                        editor.putString("Player Id", playerId);
                                                        editor.putBoolean("Verified", false);
                                                        editor.apply();

                                                        progressDialog.dismiss();

                                                        Toast.makeText(SignUpActivity.this, "Welcome!!", Toast.LENGTH_SHORT).show();

                                                        //Send Email Verification
                                                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(SignUpActivity.this, "A Verification Link Has Been Sent To Your Email...", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                    }
                                                    else
                                                        {
                                                        progressDialog.dismiss();
                                                        if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                                            Toast.makeText(SignUpActivity.this, "Email Id Is Already Registered!!", Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(SignUpActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Log.i("yoyo2",e.toString());
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(SignUpActivity.this, "Scholar Number And Registered Email To Not Match!!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                            else {
                                Toast.makeText(SignUpActivity.this, "Please Enter A Valid Scholar Number!!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Toast.makeText(getApplicationContext(),"Hi",Toast.LENGTH_SHORT).show();
    }
}
