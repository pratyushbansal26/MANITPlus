package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EventFragment extends Fragment{
    private RecyclerAdapter_Events adapter;
    public ArrayList<EventClass> list;
    private RecyclerView programminglist;
    private ProgressBar progressBar;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton Addbutton;
    public static String mtitle;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    private DatabaseReference root = db.getReference().child("Colleges").child("MANIT").child("Events");


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event,container,false);

        MainActivity.toolbar.setTitle("Events");

        Addbutton = view.findViewById(R.id.addbutton);
        mSwipeRefreshLayout = view.findViewById(R.id.refreshlayout);
        progressBar = view.findViewById(R.id.progressBar2);
        programminglist = view.findViewById(R.id.programminglist);

        if(!MainActivity.Admin) {
            Addbutton.hide();
            Addbutton.setEnabled(false);
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onFetch();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        Addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new EventUploadFragment()).commit();
            }
        });

        onFetch();

        return view;

    }

    public void onFetch(){

        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    EventClass model = dataSnapshot.getValue(EventClass.class);
                    list.add(model);
                }

                //Sort List
                try {
                    list = Sort(list);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                progressBar.setVisibility(View.INVISIBLE);

                adapter = new RecyclerAdapter_Events(getContext(), list);
                programminglist.setLayoutManager(new LinearLayoutManager(getContext()));
                programminglist.setAdapter(adapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    class Comparator implements java.util.Comparator<EventClass>
    {
        public int compare(EventClass model1, EventClass model2){

            try {
                return mSimpleDateFormat.parse(model1.getDate()).compareTo(mSimpleDateFormat.parse(model2.getDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    //Function To Sort The List By Date
    ArrayList<EventClass> Sort(ArrayList<EventClass> arrayList) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date today1 = new Date();
        String s = dateFormat.format(today1);

        Date today = dateFormat.parse(s);

        ArrayList<EventClass> active = new ArrayList<>();
        ArrayList<EventClass> old = new ArrayList<>();

        for(int i=0;i<arrayList.size();i++)
        {
            Date date = dateFormat.parse(arrayList.get(i).getDate());
            if(date.compareTo(today)>=0)
                active.add(arrayList.get(i));
            else
                old.add(arrayList.get(i));
        }
        active.sort(new Comparator());
        old.sort(new Comparator());

        ArrayList<EventClass> pdfArrayList = new ArrayList<>();
        for(int i=0;i<active.size();i++)
            pdfArrayList.add(active.get(i));
        for(int i=old.size()-1;i>=0;i--)
            pdfArrayList.add(old.get(i));

        return pdfArrayList;
    };

}