package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessFragment extends Fragment {

    private TextView breakfast , lunch , snacks , dinner;
    private Spinner daysOfWeek;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mess,container,false);

        breakfast = view.findViewById(R.id.breakfast);
        lunch = view.findViewById(R.id.lunch);
        snacks = view.findViewById(R.id.snacks);
        dinner = view.findViewById(R.id.dinner);
        daysOfWeek = view.findViewById(R.id.dayOfWeek);

        ArrayList<String> days = new ArrayList<>();
        days.add("Monday");days.add("Tuesday");days.add("Wednesday");days.add("Thursday");days.add("Friday");days.add("Saturday");
        days.add("Sunday");
        ArrayAdapter<String> days_spinner = new ArrayAdapter<>(getActivity() , android.R.layout.simple_spinner_dropdown_item
                ,days);
        daysOfWeek.setAdapter(days_spinner);

        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEEE" , Locale.ENGLISH);
        String dayToday = dateFormat.format(today);

        FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Mess")
                .child("H-10").child(dayToday).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                breakfast.setText(snapshot.child("Breakfast").getValue().toString());
                lunch.setText(snapshot.child("Lunch").getValue().toString());
                snacks.setText(snapshot.child("Snacks").getValue().toString());
                dinner.setText(snapshot.child("Dinner").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        int spinnerPosition = days_spinner.getPosition(dayToday);
        daysOfWeek.setSelection(spinnerPosition);

        daysOfWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Mess").child("H-10")
                        .child(daysOfWeek.getSelectedItem().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        breakfast.setText(snapshot.child("Breakfast").getValue().toString());
                        lunch.setText(snapshot.child("Lunch").getValue().toString());
                        snacks.setText(snapshot.child("Snacks").getValue().toString());
                        dinner.setText(snapshot.child("Dinner").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }
}