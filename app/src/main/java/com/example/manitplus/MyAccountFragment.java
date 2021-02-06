package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class MyAccountFragment extends Fragment {

    private TextView user_name , scholar_no , email , residential_status , hostel , branch , year , section;
    private CircleImageView profile_pic;
    private FloatingActionMenu edit_pic;


    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account,container,false);

        user_name = view.findViewById(R.id.user_name);
        scholar_no = view.findViewById(R.id.sch_no);
        email = view.findViewById(R.id.email);
        residential_status = view.findViewById(R.id.status);
        hostel = view.findViewById(R.id.hostel);
        branch = view.findViewById(R.id.branch);
        year = view.findViewById(R.id.year);
        section = view.findViewById(R.id.section);
        profile_pic = view.findViewById(R.id.profile_pic);
        edit_pic = view.findViewById(R.id.edit_pic);

        MainActivity.toolbar.setTitle("My Account");

        FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Users")
                .child(ScholarNo).child("Profile Pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Glide.with(getContext()).load(snapshot.getValue().toString()).into(profile_pic);
                }
                else {
                    profile_pic.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT")
                .child("Users").child(ScholarNo);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s;
                s = snapshot.child("Name").getValue().toString();
                user_name.setText(s);
                s = snapshot.child("Email").getValue().toString();
                email.setText("Email Id : "+ s);
                s = snapshot.child("Scholar No").getValue().toString();
                scholar_no.setText("Scholar Number : "+s);
                s = snapshot.child("Branch").getValue().toString();
                branch.setText("Branch : "+s);
                s = snapshot.child("Section").getValue().toString();
                section.setText("Section : "+s);
                s = snapshot.child("Year").getValue().toString();
                year.setText("Year : "+ s);
                if(!snapshot.child("Hostel").getValue().toString().equals("Day Scholar")) {
                    residential_status.setText("Residential Status : Hosteler");
                    s = snapshot.child("Hostel").getValue().toString();
                    hostel.setText("Hostel : " + s);
                }
                else {
                    residential_status.setText("Residential Status : Day Scholar");
                    hostel.setText("");
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        edit_pic.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfilePic();
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new ProfilePicDisplayFragment()).commit();

            }
        });

        return view;
    }

    private void editProfilePic() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Profile Picture");
        builder.setMessage("Update Your Profile Picture");
        builder.setPositiveButton("NEW PROFILE PHOTO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent , "Select Image") , 1);
            }
        });
        builder.setNegativeButton("REMOVE PROFILE PHOTO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Users")
                        .child(ScholarNo).child("Profile Pic").removeValue();
                FirebaseStorage.getInstance().getReference().child("Colleges").child("MANIT").child("Profile_Pics").child(ScholarNo).delete();
                profile_pic.setImageResource(R.drawable.ic_account_circle_black_24dp);
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
            uploadProfilePic(data.getData());

    }

    private void uploadProfilePic(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading Profile Pic");
        progressDialog.setMessage("0% uploaded");
        progressDialog.show();

        FirebaseStorage.getInstance().getReference().child("Colleges").child("MANIT").child("Profile_Pics").child(ScholarNo).putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog.dismiss();
                        Toast.makeText(getContext() , "Successfully Updated" , Toast.LENGTH_SHORT).show();

                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uri.isComplete());
                        Uri url = uri.getResult();
                        FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Users").child(ScholarNo)
                                .child("Profile Pic").setValue(url.toString());
                        Glide.with(getContext()).load(url.toString()).into(profile_pic);
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progress = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage((int) progress + "% Uploaded...");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Unable to Upload" , Toast.LENGTH_SHORT).show();
            }
        });

    }
}