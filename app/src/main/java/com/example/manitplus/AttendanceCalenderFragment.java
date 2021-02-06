package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AttendanceCalenderFragment extends Fragment {

    private CalendarView calendar;
    private TextView present, absent, date_selected;
    private Button present_plus, present_subtract, absent_plus, absent_subtract;
    private int pre = 0, abs = 0;
    static public String subject;

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_calender, container, false);

        calendar = view.findViewById(R.id.calendar);
        present = view.findViewById(R.id.present);
        absent = view.findViewById(R.id.absent);
        present_plus = view.findViewById(R.id.present_plus);
        present_subtract = view.findViewById(R.id.present_subtract);
        absent_plus = view.findViewById(R.id.absent_plus);
        absent_subtract = view.findViewById(R.id.absent_subtract);
        date_selected = view.findViewById(R.id.date_selected);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date_selected.setText(simpleDateFormat.format(new Date()));
        calendar.setDate(new Date().getTime());
        displayAttendance(MainActivity.ScholarNo, subject, simpleDateFormat.format(new Date()));

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                String day = "";
                month += 1;
                if (dayOfMonth < 10) day += "0";
                day += (dayOfMonth + "-");
                if (month < 10) day += "0";
                day += (month + "-" + year);
                displayAttendance(MainActivity.ScholarNo, subject, day);
            }
        });
        return view;
    }

    public void present_absent(String sch_no, String subject, String day) {

        DatabaseReference date = FirebaseDatabase.getInstance().getReference().child("Colleges").
                child("MANIT").child(Year)
                .child(Branch).child(Section).child("Attendance").child(sch_no).child(subject).child(day);
        date.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    present.setText("No Data");
                    absent.setText("No Data");
                    Toast.makeText(getContext(), "No Data", Toast.LENGTH_SHORT).show();
                } else {

                    pre = 0;
                    abs = 0;

                    for (DataSnapshot time : snapshot.getChildren()) {
                        if (time.getValue().toString().equals("P")) pre++;
                        else abs++;
                    }
                    present.setText("Present Count : " + pre);
                    absent.setText("Absent Count : " + abs);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void displayAttendance(String sch_no, String subject, String day) {
        date_selected.setText(day);
        present_absent(MainActivity.ScholarNo, subject, day);

        final String finalDay = day;

        present_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date today = new Date();
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                String time = df.format(today);

                FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").
                        child(MainActivity.Year).child(MainActivity.Branch).child(MainActivity.Section).child("Attendance").
                        child(MainActivity.ScholarNo).child(subject).child(finalDay).child(time).setValue("P");

                present_absent(MainActivity.ScholarNo, subject, finalDay);


                Toast.makeText(getContext(), "Present Added", Toast.LENGTH_SHORT).show();
            }
        });

        absent_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date today = new Date();
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
                String time = df.format(today);

                FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").
                        child(MainActivity.Year).child(MainActivity.Branch).child(MainActivity.Section).child("Attendance").
                        child(MainActivity.ScholarNo).child(subject).child(finalDay).child(time).setValue("A");

                present_absent(MainActivity.ScholarNo, subject, finalDay);

                Toast.makeText(getContext(), "Absent Added", Toast.LENGTH_SHORT).show();
            }
        });

        present_subtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Colleges").
                        child("MANIT").child(MainActivity.Year).child(MainActivity.Branch).child(MainActivity.Section).
                        child("Attendance").child(MainActivity.ScholarNo).child(subject).child(finalDay);
                reff.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int flag = 0;

                        for (DataSnapshot time : snapshot.getChildren()) {
                            if (time.getValue().toString().equals("P")) {
                                flag = 1;
                                reff.child(time.getKey()).removeValue();
                                break;
                            }
                        }

                        if (flag == 0)
                            Toast.makeText(getContext(), "No data Found", Toast.LENGTH_SHORT).show();
                        else {
                            present_absent(MainActivity.ScholarNo, subject, finalDay);
                            Toast.makeText(getContext(), "Present Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        absent_subtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Colleges").
                        child("MANIT").child(MainActivity.Year).child(MainActivity.Branch).child(MainActivity.Section).
                        child("Attendance").child(MainActivity.ScholarNo).child(subject).child(finalDay);
                reff.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int flag = 0;

                        for (DataSnapshot time : snapshot.getChildren()) {
                            if (time.getValue().toString().equals("A")) {
                                flag = 1;
                                reff.child(time.getKey()).removeValue();
                                break;
                            }
                        }
                        if (flag == 0)
                            Toast.makeText(getContext(), "No data Found", Toast.LENGTH_SHORT).show();
                        else {
                            present_absent(MainActivity.ScholarNo, subject, finalDay);
                            Toast.makeText(getContext(), "Absent Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}