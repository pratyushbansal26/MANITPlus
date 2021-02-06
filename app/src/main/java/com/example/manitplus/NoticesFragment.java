package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class NoticesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionMenu floatingActionMenu;
    private ProgressBar loading;
    private SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notices, container, false);


        //Make Folders
        File CollegeApp = new File(Environment.getExternalStorageDirectory(), "MANIT+");
        if (!CollegeApp.exists())
            CollegeApp.mkdir();
        File Assignments = new File(CollegeApp.getAbsolutePath(), "Notices");
        if (!Assignments.exists())
            Assignments.mkdir();


        recyclerView = view.findViewById(R.id.RecyclerView2);
        floatingActionMenu = view.findViewById(R.id.FloatingButton);
        loading = view.findViewById(R.id.Loading);
        swipeRefreshLayout = view.findViewById(R.id.SwipeRefreshLayout);

        MainActivity.toolbar.setTitle("Notices");

        //Hide Button
        if(!MainActivity.Admin)
            floatingActionMenu.hideMenuButton(false);

        //Fill The Adapter & Recycler
        displayNotices();

        //Upload Notice
        floatingActionMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Notice Upload Fragment
                if(MainActivity.Admin)
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new NoticeUploadFragment()).commit();
                else
                    Toast.makeText(getContext(),"Action Not Allowed!!\n(You Are Not An Admin)",Toast.LENGTH_SHORT).show();
            }
        });


        //Hide Button On Scroll
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0)
                    floatingActionMenu.hideMenu(true);
                else
                    floatingActionMenu.showMenu(true);
            }
        });

        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayNotices();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;

    }

    //Notice Class
    public class Notice {
        public String name, url, pages, uploader, uploaddate, size,description;

        public Notice(String name,String description, String url, String uploader, String pages, String uploaddate, String size) {
            this.name = name;
            this.description = description;
            this.url = url;
            this.uploader = uploader;
            this.pages = pages;
            this.uploaddate = uploaddate;
            this.size = size;
        }
    }

    //Function to Fill RecyclerView Notices
    void displayNotices()
    {
        final DatabaseReference Notices = root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Notices");
        Notices.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Notice> noticeArrayList = new ArrayList<>();

                    for (DataSnapshot pdf : dataSnapshot.getChildren())
                    {
                        String name = pdf.child("File Name").getValue().toString();
                        String description = pdf.child("Description").getValue().toString();
                        String uploader = pdf.child("Uploader").getValue().toString();
                        String url = pdf.child("URL").getValue().toString();
                        String pages = pdf.child("Pages").getValue().toString() + " Pages";
                        String uploaddate = pdf.child("Upload Date").getValue().toString();
                        String size = "(" + pdf.child("Size").getValue().toString() + ")";
                        noticeArrayList.add(new Notice(name, description, url, uploader, pages, uploaddate, size));
                    }

                noticeArrayList.sort(new Comparator());
                loading.setVisibility(View.INVISIBLE);
                RecyclerAdapter_Notices recyclerAdapter_notices = new RecyclerAdapter_Notices(getContext(), noticeArrayList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(recyclerAdapter_notices);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Comparator Class
    class Comparator implements java.util.Comparator<Notice>
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        public int compare(Notice notice1, Notice notice2)
        {
            try {
                return -1*(dateFormat.parse(notice1.uploaddate).compareTo(dateFormat.parse(notice2.uploaddate)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}
