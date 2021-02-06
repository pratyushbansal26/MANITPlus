package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NotesFragment extends Fragment {

    private RecyclerView mNotesRecycler;
    //String Subject_Name;
    private FloatingActionMenu mFloatingActionButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Spinner subjects_spinner;
    private ProgressBar loading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes,container,false);

        //Subject on which user clicked.
        //Subject_Name = getIntent().getExtras().getString("Subject_Name").toString();
        mNotesRecycler = view.findViewById(R.id.notes_list);
        mFloatingActionButton = view.findViewById(R.id.floating);
        loading = view.findViewById(R.id.Loading);
        mSwipeRefreshLayout = view.findViewById(R.id.refresher);
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLUE , Color.GREEN , Color.YELLOW);
        subjects_spinner = view.findViewById(R.id.subject_spinner);

        MainActivity.toolbar.setTitle("Notes");


        //Create Folders
        File mFile2 = new File(Environment.getExternalStorageDirectory()+"/MANIT+" , "Notes");
        if(!mFile2.exists()) mFile2.mkdirs();
        DatabaseReference Notes1 = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year)
                .child(MainActivity.Branch).child("Subjects");
        Notes1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot subject : snapshot.getChildren()) {
                    File mFile = new File(Environment.getExternalStorageDirectory()+"/MANIT+/Notes" , subject.getKey());
                    if(!mFile.exists())
                        mFile.mkdirs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year)
                .child(MainActivity.Branch).child("Subjects");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> mArrayList = new ArrayList<>();
                mArrayList.add("All Subjects");
                for(DataSnapshot subs : snapshot.getChildren())
                {
                    String s = subs.getKey().toString();
                    mArrayList.add(s);
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item , mArrayList);
                subjects_spinner.setAdapter(spinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        RecyclerData("All Subjects");

        subjects_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RecyclerData(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mFloatingActionButton.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new NotesUploadFragment()).commit();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mSwipeRefreshLayout.setRefreshing(true);
                RecyclerData(subjects_spinner.getSelectedItem().toString());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;

    }

    public void RecyclerData(final String subject) {

        final ArrayList<Model2> mArrayList2 = new ArrayList<>();

        if(subject.equals("All Subjects"))
        {

            DatabaseReference Subject = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year)
                    .child(MainActivity.Branch).child(MainActivity.Section).child("Notes");
            Subject.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot subs : snapshot.getChildren()) {
                        for(DataSnapshot pdfs : subs.getChildren())
                        {
                            Model2 m = new Model2(pdfs.child("author").getValue().toString(),
                                    pdfs.child("day").getValue().toString(),
                                    (long) pdfs.child("users").getChildrenCount(),
                                    (long) pdfs.child("pages").getValue(),
                                    pdfs.child("pdf_name").getValue().toString(),
                                    pdfs.child("size").getValue().toString(),
                                    subs.getKey(), pdfs.child("url").getValue().toString());
                            mArrayList2.add(m);
                        }
                    }

                    Collections.sort(mArrayList2, new Comparator<Model2>() {
                        @Override
                        public int compare(Model2 o1, Model2 o2) {

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                            try {
                                return simpleDateFormat.parse(o1.getDay()).compareTo(simpleDateFormat.parse(o2.getDay()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 1;
                        }
                    });
                    Collections.reverse(mArrayList2);

                    RecyclerAdapter_Notes notesListAdapter = new RecyclerAdapter_Notes(getActivity(), mArrayList2);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    mNotesRecycler.setLayoutManager(layoutManager);
                    loading.setVisibility(View.INVISIBLE);
                    mNotesRecycler.setAdapter(notesListAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        else {
            DatabaseReference Subject = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year)
                    .child(MainActivity.Branch).child(MainActivity.Section).child("Notes").child(subject);
            Subject.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot pdfs : snapshot.getChildren()) {

                        Model2 m = new Model2(pdfs.child("author").getValue().toString(),
                                pdfs.child("day").getValue().toString(),
                                (long) pdfs.child("users").getChildrenCount(),
                                (long) pdfs.child("pages").getValue(),
                                pdfs.child("pdf_name").getValue().toString(),
                                pdfs.child("size").getValue().toString(),
                                subject, pdfs.child("url").getValue().toString());
                        mArrayList2.add(m);
                    }

                    Collections.sort(mArrayList2, new Comparator<Model2>() {
                        @Override
                        public int compare(Model2 o1, Model2 o2) {

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                            try {
                                return simpleDateFormat.parse(o1.getDay()).compareTo(simpleDateFormat.parse(o2.getDay()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 1;
                        }
                    });
                    Collections.reverse(mArrayList2);

                    RecyclerAdapter_Notes notesListAdapter = new RecyclerAdapter_Notes(getActivity(), mArrayList2);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    mNotesRecycler.setLayoutManager(layoutManager);
                    mNotesRecycler.setAdapter(notesListAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    public class Model2 {

        String subject_name, pdf_name, author , url , size;
        long pages, likes ;
        String day;

        Model2(String author, String day , long likes , long  pages, String pdf_name , String  size , String subject_name , String url )
        {
            this.pdf_name=pdf_name ;this.subject_name = subject_name;this. author= author;this. day= day;
            this. pages= pages;this. size= size;this.url = url;this.likes = likes;
        }

        public String getDay() {
            return day;
        }
    }

}