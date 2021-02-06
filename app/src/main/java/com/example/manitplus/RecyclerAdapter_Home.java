package com.example.manitplus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class RecyclerAdapter_Home extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<HomeFragment.HomeTimetableItem> TimetableArray;

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    private Date today = new Date();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private int min=100,max=0,lowattendance=0,daysp=0,dayst=0;
    private int completed=0,total=0;
    Date recentdate;
    private String minsubject="",maxsubject="";
    Date todaydate;


    public RecyclerAdapter_Home(Context context, ArrayList<HomeFragment.HomeTimetableItem> TimetableArray)
    {
        this.context=context;
        this.TimetableArray = TimetableArray;

        if(dayOfWeek==1)
            dayOfWeek=7;
        else
            dayOfWeek--;
        String s = dateFormat.format(today);
        try {
            todaydate = dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh = null;
        if(viewType==1) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.home_attendance_card, parent, false);
            vh = new AttendanceHolder1(view);
        }
        else if(viewType==2)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.home_timetable_card, parent, false);
            vh = new TimetableHolder(view);
        }
        else if(viewType==3)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.home_assignment_card, parent, false);
            vh = new AssignmentHolder(view);
        }
        else if(viewType==4)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.home_event_card, parent, false);
            vh = new EventHolder(view);
        }
        else if(viewType==5)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.home_notice_card, parent, false);
            vh = new NoticeHolder(view);
        }
        else if(viewType==6)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.home_announcement_card, parent, false);
            return new AnnouncementHolder(view);
        }
        return vh;
    }


    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vholder, int position) {

        if(getItemViewType(position)==1) {

            min=101;
            max=lowattendance=daysp=dayst=0;
            minsubject=maxsubject="";
            final AttendanceHolder1 holder = (AttendanceHolder1) vholder;

            FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Attendance").child(ScholarNo)
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                     SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                     int attend=0,leave=0;

                    for(DataSnapshot subject : dataSnapshot.getChildren())
                    {
                        int present=0,absent=0;
                        for(DataSnapshot date : subject.getChildren())
                        {
                            for(DataSnapshot time : date.getChildren()) {
                                if (time.getValue().toString().equals("P"))
                                    present++;
                                else if (time.getValue().toString().equals("A"))
                                    absent++;
                                Log.i("hello",date.getKey().toString());
                                try {
                                    if((today.getTime()-dateFormat.parse(date.getKey().toString()).getTime())/(1000*60*60*24)<dayOfWeek) {
                                        dayst++;
                                        if(time.getValue().toString().equals("P"))
                                            daysp++;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    Log.i("Hello",e.toString());
                                }
                            }
                        }
                        int percent = (int)(100.0*present)/(present+absent);
                        if(percent<75)
                            lowattendance++;
                        if(percent<min)
                        {
                            min = percent;
                            minsubject = subject.getKey();
                        }
                        if(percent>max)
                        {
                            max=percent;
                            maxsubject=subject.getKey();
                        }

                        if(percent<75)
                        {
                            int c=0;
                            while((float)(present+c)/(present+absent+c)<0.75)
                                c++;
                            attend+=c;
                        }
                        else
                        {
                            int c=0;
                            while((float)(present)/(present+absent+c)>=0.75)
                                c++;
                            c--;
                            leave+=c;
                        }

                    }
                    if(min==101)
                        min=0;

                    holder.status.setText(""+lowattendance+" Subjects");
                    holder.min.setText(""+min+"%");
                    holder.max.setText(""+max+"%");
                    holder.minsubject.setText("("+minsubject+")");
                    holder.maxsubject.setText("("+maxsubject+")");
                    holder.classes.setText(""+daysp+"/"+dayst+" Attended");
                    holder.attend.setText("Min Classes To Attend : "+attend);
                    holder.leave.setText("Max Leaves Possible: "+leave);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.navigationView.setCheckedItem(R.id.AttendanceMenu);
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new AttendanceFragment()).commit();
                }
            });
        }

        else if(getItemViewType(position)==2) {

            TimetableHolder holder = (TimetableHolder) vholder;
            RecyclerAdapter_HomeTimetable adapter = new RecyclerAdapter_HomeTimetable(context, TimetableArray);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setAdapter(adapter);
            if(TimetableArray.size()==0)
                holder.holiday.setText("No Classes Today!! \uD83E\uDD73");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new AttendanceFragment()).commit();
                }
            });
        }

        else if(getItemViewType(position)==3) {

            completed=total=0;
            final AssignmentHolder holder = (AssignmentHolder) vholder;
            root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Assignments").child("Questions").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                            String recentSubject="__",status="__";
                            int thisweek=0;
                            Date recentdate = new Date();
                            try {
                                recentdate = dateFormat.parse("01/01/2025");
                                String s = dateFormat.format(today);
                                today = dateFormat.parse(s);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            for(DataSnapshot subject : datasnapshot.getChildren()) {
                                for (DataSnapshot pdf : subject.getChildren()) {
                                    String date = pdf.child("Submission Date").getValue().toString();
                                    try {
                                        if (dateFormat.parse(date).compareTo(today) >= 0) {
                                            total++;
                                            if (pdf.child("Users").child(ScholarNo).exists())
                                                completed++;
                                            if(dateFormat.parse(date).compareTo(recentdate)<=0)
                                            {
                                                recentdate = dateFormat.parse(date);
                                                if(pdf.child("Users").child(ScholarNo).exists())
                                                    status="Completed";
                                                else
                                                    status = "Pending";
                                                recentSubject = subject.getKey().toString();
                                            }
                                            Log.i("Hello",""+dayOfWeek);
                                            if((dateFormat.parse(date).getTime()-today.getTime())/(1000*24*60*60)<=(7-dayOfWeek)) {
                                                thisweek++;
                                                Log.i("Hello",date);
                                            }
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        Log.i("hello",e.toString());
                                    }
                                }
                            }
                            holder.complete.setText(""+completed);
                            holder.available.setText(""+total);
                            if(thisweek==1)
                                holder.thisweek.setText(""+thisweek+" Submission Scheduled");
                            else
                                holder.thisweek.setText(""+thisweek+" Submissions Scheduled");

                            if(dateFormat.format(recentdate).equals("01/01/2025"))
                                holder.recentdate.setText("__");
                            else {
                                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy, EEEE");
                                holder.recentdate.setText(dateFormat1.format(recentdate));
                            }

                            holder.recentsubject.setText(recentSubject);
                            holder.status.setText(status);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            holder.itemview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.navigationView.setCheckedItem(R.id.AssignmentMenu);
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new AssignmentFragment()).commit();
                }
            });

        }

        else if(getItemViewType(position)==4) {

            final EventHolder holder = (EventHolder) vholder;
            root.child("Colleges").child("MANIT").child("Events").
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                            int liked=0,upcoming=0,thisweek=0;
                            String society="",title="";
                            Date recentdate = new Date();
                            try {
                                recentdate = dateFormat.parse("01/01/2025");
                                String s = dateFormat.format(today);
                                today = dateFormat.parse(s);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            for(DataSnapshot event : datasnapshot.getChildren()) {
                                String date = event.child("date").getValue().toString();
                                try {
                                    if (dateFormat.parse(date).compareTo(today) >= 0) {
                                        upcoming++;
                                        if (event.child("Likes").child(ScholarNo).exists())
                                            liked++;
                                        if (dateFormat.parse(date).compareTo(recentdate) <= 0)
                                        {
                                            recentdate = dateFormat.parse(date);
                                            title = event.child("title").getValue().toString();
                                            society = event.child("societyname").getValue().toString();
                                        }
                                        if ((dateFormat.parse(date).getTime() - today.getTime()) / (1000 * 24 * 60 * 60) <= (7 - dayOfWeek)) {
                                            thisweek++;
                                            Log.i("Hello", date);
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    Log.i("hello", e.toString());
                                }
                            }
                            holder.liked.setText(""+liked);
                            holder.upcoming.setText(""+upcoming);
                            if(thisweek==1)
                                holder.thisweek.setText(""+thisweek+" Event Upcoming");
                            else
                                holder.thisweek.setText(""+thisweek+" Events Upcoming");

                            if(dateFormat.format(recentdate).equals("01/01/2025")) {
                                holder.recent.setText("No Events Available");
                                holder.date.setText("_");
                                holder.society.setText("_");
                            }
                            else
                            {
                                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy, EEEE");
                                holder.recent.setText(title);
                                holder.society.setText(society);
                                holder.date.setText(dateFormat1.format(recentdate));
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            holder.itemview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.navigationView.setCheckedItem(R.id.AssignmentMenu);
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new EventFragment()).commit();
                }
            });

        }

        else if(getItemViewType(position)==5) {

            final NoticeHolder holder = (NoticeHolder) vholder;
            root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Notices").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    ArrayList<announcement> arrayList = new ArrayList<>();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()) {

                        try {
                            arrayList.add(new announcement(dataSnapshot.child("File Name").getValue().toString(),dateFormat1.parse(dataSnapshot.child("Upload Date").getValue().toString())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        arrayList.sort(new Comparator());

                        if(arrayList.size()>=1)
                        {
                            holder.a1.setText(arrayList.get(0).title);
                            try {
                                if(todaydate.compareTo(dateFormat.parse(dateFormat.format(arrayList.get(0).date)))==0)
                                    holder.t1.setText(timeFormat.format(arrayList.get(0).date));
                                else
                                    holder.t1.setText(dateFormat1.format(arrayList.get(0).date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            holder.a1.setText("__");
                            holder.t1.setText("__");
                        }

                        if(arrayList.size()>=2)
                        {
                            holder.a2.setText(arrayList.get(1).title);
                            try {
                                if(todaydate.compareTo(dateFormat.parse(dateFormat.format(arrayList.get(1).date)))==0)
                                    holder.t2.setText(timeFormat.format(arrayList.get(1).date));
                                else
                                    holder.t2.setText(dateFormat1.format(arrayList.get(1).date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            holder.a2.setText("__");
                            holder.t2.setText("__");
                        }
                        if(arrayList.size()>=3)
                        {
                            holder.a3.setText(arrayList.get(2).title);
                            try {
                                if(todaydate.compareTo(dateFormat.parse(dateFormat.format(arrayList.get(2).date)))==0)
                                    holder.t3.setText(timeFormat.format(arrayList.get(2).date));
                                else
                                    holder.t3.setText(dateFormat1.format(arrayList.get(2).date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            holder.a3.setText("__");
                            holder.t3.setText("__");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            holder.itemview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.navigationView.setCheckedItem(R.id.NoticesMenu);
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new NoticesFragment()).commit();
                }
            });
        }

        else if(getItemViewType(position)==6) {

            final AnnouncementHolder holder = (AnnouncementHolder) vholder;
            root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Announcements").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    ArrayList<announcement> arrayList = new ArrayList<>();
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()) {

                        try {
                            arrayList.add(new announcement(dataSnapshot.child("Title").getValue().toString(),dateFormat1.parse(dataSnapshot.child("Upload Date").getValue().toString())));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        arrayList.sort(new Comparator());

                        if(arrayList.size()>=1)
                        {
                            holder.a1.setText(arrayList.get(0).title);
                            try {
                                if(todaydate.compareTo(dateFormat.parse(dateFormat.format(arrayList.get(0).date)))==0)
                                    holder.t1.setText(timeFormat.format(arrayList.get(0).date));
                                else
                                    holder.t1.setText(dateFormat1.format(arrayList.get(0).date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            holder.a1.setText("__");
                            holder.t1.setText("__");
                        }

                        if(arrayList.size()>=2)
                        {
                            holder.a2.setText(arrayList.get(1).title);
                            try {
                                if(todaydate.compareTo(dateFormat.parse(dateFormat.format(arrayList.get(1).date)))==0)
                                    holder.t2.setText(timeFormat.format(arrayList.get(1).date));
                                else
                                    holder.t2.setText(dateFormat1.format(arrayList.get(1).date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            holder.a2.setText("__");
                            holder.t2.setText("__");
                        }
                        if(arrayList.size()>=3)
                        {
                            holder.a3.setText(arrayList.get(2).title);
                            try {
                                if(todaydate.compareTo(dateFormat.parse(dateFormat.format(arrayList.get(2).date)))==0)
                                    holder.t3.setText(timeFormat.format(arrayList.get(2).date));
                                else
                                    holder.t3.setText(dateFormat1.format(arrayList.get(2).date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            holder.a3.setText("__");
                            holder.t3.setText("__");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            holder.itemview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.navigationView.setCheckedItem(R.id.AnnouncementsMenu);
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new AnnouncementsFragment()).commit();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        return  position+1;
    }

    private class TimetableHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        TextView holiday;
        View itemView;

        public TimetableHolder(@NonNull View itemView) {
            super(itemView);
            this.recyclerView = itemView.findViewById(R.id.TimetableRecyclerView);
            this.itemView = itemView;
            holiday = itemView.findViewById(R.id.HolidayHome);
        }

    }

    private class AttendanceHolder1 extends RecyclerView.ViewHolder {

        @NonNull
        private final View itemView;
        TextView status,min,max,minsubject,maxsubject,classes,attend,leave;

        public AttendanceHolder1(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.status = itemView.findViewById(R.id.Status);
            this.min = itemView.findViewById(R.id.Min);
            this.max = itemView.findViewById(R.id.Max);
            this.minsubject = itemView.findViewById(R.id.MinSubject);
            this.maxsubject = itemView.findViewById(R.id.MaxSubject);
            this.classes = itemView.findViewById(R.id.Classes);
            attend = itemView.findViewById(R.id.attend);
            leave = itemView.findViewById(R.id.leave);
        }

    }

    private class AssignmentHolder extends RecyclerView.ViewHolder {

        TextView complete,recentdate,thisweek,available,status,recentsubject;
        View itemview;

        public AssignmentHolder(@NonNull View itemView) {
            super(itemView);
            this.complete = itemView.findViewById(R.id.Completed);
            this.recentdate = itemView.findViewById(R.id.RecentDate);
            this.recentsubject = itemView.findViewById(R.id.RecentSubject);
            this.status = itemView.findViewById(R.id.Status);
            this.thisweek = itemView.findViewById(R.id.ThisWeek);
            this.available= itemView.findViewById(R.id.Available1);
            itemview = itemView;
        }

    }

    private class EventHolder extends RecyclerView.ViewHolder {

        TextView upcoming,recent,thisweek,liked,society,date;
        View itemview;

        public EventHolder(@NonNull View itemView) {
            super(itemView);
            this.liked = itemView.findViewById(R.id.Completed);
            this.recent = itemView.findViewById(R.id.Recent);
            this.society = itemView.findViewById(R.id.Society);
            this.thisweek = itemView.findViewById(R.id.ThisWeek);
            this.upcoming= itemView.findViewById(R.id.Available1);
            this.date = itemView.findViewById(R.id.EventDate);
            itemview = itemView;
        }

    }

    private class AnnouncementHolder extends RecyclerView.ViewHolder {

        TextView a1,a2,a3,t1,t2,t3;
        View itemview;

        public AnnouncementHolder(@NonNull View itemView) {
            super(itemView);
            this.a1 = itemView.findViewById(R.id.A1);
            this.a2 = itemView.findViewById(R.id.A2);
            this.a3 = itemView.findViewById(R.id.A3);
            this.t1 = itemView.findViewById(R.id.T1);
            this.t2 = itemView.findViewById(R.id.T2);
            this.t3 = itemView.findViewById(R.id.T3);
            itemview = itemView;
        }

    }

    private class NoticeHolder extends RecyclerView.ViewHolder {

        TextView a1,a2,a3,t1,t2,t3;
        View itemview;

        public NoticeHolder(@NonNull View itemView) {
            super(itemView);
            this.a1 = itemView.findViewById(R.id.A1);
            this.a2 = itemView.findViewById(R.id.A2);
            this.a3 = itemView.findViewById(R.id.A3);
            this.t1 = itemView.findViewById(R.id.T1);
            this.t2 = itemView.findViewById(R.id.T2);
            this.t3 = itemView.findViewById(R.id.T3);
            itemview = itemView;
        }

    }

    private class announcement
    {
        String title;
        Date date;
        public  announcement(String title, Date date)
        {
            this.date=date;
            this.title=title;
        }
    }

    //Comparator Class For Announcement Sort
    private class Comparator implements java.util.Comparator<announcement>
    {
        public int compare(announcement a1, announcement a2) {
            return  -1*(a1.date.compareTo(a2.date));
        }
    }
}
