package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfilePicDisplayFragment extends Fragment {

    ImageView profile_pic_display;

    String ScholarNo = MainActivity.ScholarNo;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_pic_display,container,false);

        profile_pic_display = view.findViewById(R.id.profile_pic_display);
        FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Users")
                .child(ScholarNo).child("Profile Pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Glide.with(getContext()).load(snapshot.getValue().toString()).into(profile_pic_display);
                }
                else {
                    profile_pic_display.setImageResource(R.drawable.ic_account_circle_black_24dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }
}