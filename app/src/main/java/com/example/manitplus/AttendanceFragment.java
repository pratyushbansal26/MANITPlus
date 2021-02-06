package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class AttendanceFragment extends Fragment {

    private RecyclerView recyclerView;
    private Spinner spinner;
    private ProgressBar LoadingProgressBar;
    static TextView holiday;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();

    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;
    String ScholarNo = MainActivity.ScholarNo;
    String Name = MainActivity.Name;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance,container,false);

        recyclerView = view.findViewById(R.id.RecyclerView);
        spinner = view.findViewById(R.id.Spinner);
        LoadingProgressBar = view.findViewById(R.id.LoadingProgressBar);
        holiday = view.findViewById(R.id.HolidayText);

        MainActivity.toolbar.setTitle("Attendance");

        ArrayList<String> SpinnerList = new ArrayList<String>(Arrays.asList("Today","All Subjects","Monday","Tuesday","Wednesday","Thursday","Friday", "Saturday"));
        ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_dropdown_item,SpinnerList);
        spinner.setAdapter(SpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String SelectedItem = spinner.getSelectedItem().toString();
                holiday.setText("");
                if(SelectedItem.equals("All Subjects"))
                    DisplayAllSubjects();
                else {
                    if(SelectedItem.equals("Today"))
                    {
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
                        String s = simpleDateFormat.format(date).toString();
                            DisplaySelectedDaySubjects(SelectedItem);
                    }
                    DisplaySelectedDaySubjects(SelectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;

    }

    public void DisplayAllSubjects()
    {
        final ArrayList<String> Subjects = new ArrayList<>();

        root.child("Colleges").child("MANIT").child(Year).child(Branch).child("Subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot subject: snapshot.getChildren())
                    Subjects.add(subject.getKey());

                root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Attendance").child(ScholarNo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        ArrayList<Subject_Item> SubjectArrayList= new ArrayList<>();

                        for(String subject : Subjects)
                        {
                            int present=0,absent=0;
                            ArrayList<Date> dateArrayList = new ArrayList<Date>();
                            HashMap<Date,Integer> dateHashMap = new HashMap<>();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            for(DataSnapshot date : dataSnapshot.child(subject).getChildren())
                            {
                                for(DataSnapshot time : date.getChildren())
                                {
                                    int k=3;
                                    if(time.getValue().toString().equals("P")) {
                                        present++;
                                        k=1;
                                    }
                                    else if(time.getValue().toString().equals("A")) {
                                        absent++;
                                        k=2;
                                    }
                                    try {
                                        Date date1 = dateFormat.parse(date.getKey().toString()+" "+time.getKey().toString());
                                        dateArrayList.add(date1);
                                        dateHashMap.put(date1,k);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            Collections.sort(dateArrayList,Collections.reverseOrder());
                            while (dateArrayList.size()>5) {
                                Date d = dateArrayList.get(dateArrayList.size() - 1);
                                dateArrayList.remove(dateArrayList.size() - 1);
                                dateHashMap.remove(d);
                            }
                            Collections.sort(dateArrayList);
                            Subject_Item s = new Subject_Item(subject,present,absent,dateArrayList,dateHashMap);
                            SubjectArrayList.add(s);
                        }
                        RecyclerAdapter_Attendance_AllSubjects recyclerAdapterAllSubjects = new RecyclerAdapter_Attendance_AllSubjects(getContext(),SubjectArrayList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(recyclerAdapterAllSubjects);
                        recyclerView.invalidate();
                        LoadingProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void DisplaySelectedDaySubjects(final String SelectedDayInput)
    {
        String SelectedDay = SelectedDayInput;
        if(SelectedDayInput.equals("Today"))
        {
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
            SelectedDay = simpleDateFormat.format(date).toString();
        }

        root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Timetable").
                child(SelectedDay).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final DataSnapshot Day = dataSnapshot;

                root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Attendance").child(ScholarNo)
                        .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        ArrayList<Period_Item> PeriodArrayList = new ArrayList<>();

                        for(DataSnapshot Time : Day.getChildren())
                        {
                            String subject = Time.getValue().toString();
                            int present=0,absent=0;
                            ArrayList<Date> dateArrayList = new ArrayList<Date>();
                            HashMap<Date,Integer> dateHashMap = new HashMap<>();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                            for(DataSnapshot date : dataSnapshot.child(subject).getChildren())
                            {
                                for(DataSnapshot time : date.getChildren())
                                {
                                    int k=3;
                                    if(time.getValue().toString().equals("P")) {
                                        present++;
                                        k=1;
                                    }
                                    else if(time.getValue().toString().equals("A")) {
                                        absent++;
                                        k=2;
                                    }
                                    try {
                                        Date date1 = dateFormat.parse(date.getKey().toString()+" "+time.getKey().toString());
                                        dateArrayList.add(date1);
                                        dateHashMap.put(date1,k);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            Collections.sort(dateArrayList,Collections.reverseOrder());
                            while (dateArrayList.size()>5) {
                                Date d = dateArrayList.get(dateArrayList.size() - 1);
                                dateArrayList.remove(dateArrayList.size() - 1);
                                dateHashMap.remove(d);
                            }
                            Collections.sort(dateArrayList);
                            Period_Item period_item = new Period_Item(subject,present,absent,Time.getKey().toString(),dateArrayList,dateHashMap);
                            PeriodArrayList.add(period_item);
                        }
                        if(SelectedDayInput.equals("Today")) {
                            RecyclerAdapter_Attendance_Today recyclerAdapter = new RecyclerAdapter_Attendance_Today(getContext(), PeriodArrayList);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(recyclerAdapter);
                            recyclerView.invalidate();
                        }
                        else
                        {
                            RecyclerAdapter_Attendance_Other_Days recyclerAdapter = new RecyclerAdapter_Attendance_Other_Days(getContext(), PeriodArrayList);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(recyclerAdapter);
                            recyclerView.invalidate();
                        }
                        LoadingProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class Subject_Item
    {
        public int present,absent;
        public String sub;
        public ArrayList<Date> dateArrayList;
        public  HashMap<Date,Integer> dateHashMap;
        Subject_Item(String sub,int present,int absent,ArrayList<Date> dateArrayList,HashMap<Date,Integer> dateHashMap)
        {
            this.sub=sub;
            this.present=present;
            this.absent=absent;
            this.dateArrayList=dateArrayList;
            this.dateHashMap = dateHashMap;
        }
    }

    public class Period_Item
    {
        public int present,absent;
        public String sub;
        public String time;
        public ArrayList<Date> dateArrayList;
        public  HashMap<Date,Integer> dateHashMap;
        Period_Item(String sub,int present,int absent,String time,ArrayList<Date> dateArrayList,HashMap<Date,Integer> dateHashMap)
        {
            this.sub=sub;
            this.present=present;
            this.absent=absent;
            this.time=time;
            this.dateArrayList=dateArrayList;
            this.dateHashMap = dateHashMap;
        }
    }

}
