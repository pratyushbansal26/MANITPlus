package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar loading;
    SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();

    String Year="2nd Year";
    String Branch ="CSE";
    String Section = "CSE2";
    String ScholarNo = "191112245";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        recyclerView = view.findViewById(R.id.HomeRecyclerView);
        loading = view.findViewById(R.id.Loading);
        swipeRefreshLayout = view.findViewById(R.id.SwipeRefreshLayout);

        MainActivity.toolbar.setTitle("MANIT+");

        getTimetable();

        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTimetable();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }


    //Timetable Data Retrieve
    public void getTimetable()
    {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        String today = simpleDateFormat.format(date).toString();

        root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Timetable").
                child(today).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<HomeTimetableItem> TimetableArrayList = new ArrayList<>();
                for(DataSnapshot time : dataSnapshot.getChildren())
                    TimetableArrayList.add(new HomeTimetableItem(time.getValue().toString(),time.getKey().toString()));

                //Calling AAdapter
                loading.setVisibility(View.INVISIBLE);
                RecyclerAdapter_Home recyclerAdapter_home = new RecyclerAdapter_Home(getContext(),TimetableArrayList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(recyclerAdapter_home);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // For TimeTable Adapter
    public class HomeTimetableItem
    {
        public String subject,time;
        public HomeTimetableItem(String subject,String time)
        {
            this.subject = subject;
            this.time=time;
        }
    }

}
