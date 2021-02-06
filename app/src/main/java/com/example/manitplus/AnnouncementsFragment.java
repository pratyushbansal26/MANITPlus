package com.example.manitplus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.example.manitplus.MainActivity.Admin;

public class AnnouncementsFragment extends Fragment {

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    private RecyclerView recyclerView;
    private FloatingActionMenu floatingmenu;
    private SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(Year).child(Branch).child(Section);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements,container,false);

        recyclerView = view.findViewById(R.id.RecyclerView);
        floatingmenu = view.findViewById(R.id.FloatingButton);
        swipeRefreshLayout = view.findViewById(R.id.SwipeRefreshLayout);

        MainActivity.toolbar.setTitle("Announcements");

        //Hide Button
        if(!Admin)
            floatingmenu.hideMenuButton(false);

        //Fill Recycler
        DisplayAnnouncements();

        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DisplayAnnouncements();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        //Upload Button Click
        floatingmenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Admin)
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new AnnouncementUploadFragment()).commit();
                else
                    Toast.makeText(getContext(),"Action Not Allowed!!\n(You Are Not An Admin)",Toast.LENGTH_SHORT).show();

            }
        });


        return view;
    }

    private void DisplayAnnouncements()
    {

        root.child("Announcements").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<AnnouncementItem> arrayList = new ArrayList<>();
                for (DataSnapshot announcement: snapshot.getChildren())
                {
                    arrayList.add(new AnnouncementItem(announcement.child("Title").getValue().toString(),announcement.child("Message").getValue().toString(),
                            announcement.child("Upload Date").getValue().toString(), announcement.child("From").getValue().toString()));
                }
                arrayList.sort(new Comparator());
                RecyclerAdapter_Announcements recyclerAdapter_announcements = new RecyclerAdapter_Announcements(getContext(),arrayList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(recyclerAdapter_announcements);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public class AnnouncementItem
    {
        String title;
        String message;
        String uploaddate;
        String from;
       public AnnouncementItem(String title,String message,String uploaddate, String from)
       {
           this.title = title;
           this.message = message;
           this.uploaddate = uploaddate;
           this.from = from;
       }
    }

    //Comparator Class For Announcement Sort
    private class Comparator implements java.util.Comparator<AnnouncementItem>
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        public int compare(AnnouncementItem a1, AnnouncementItem a2) {
            try {
                return  -1*(dateFormat.parse(a1.uploaddate).compareTo(dateFormat.parse(a2.uploaddate)));
            } catch (ParseException e) {
                e.printStackTrace();
                Log.i("Hello",e.toString());
            }
            return 1;
        }
    }}
