package com.example.manitplus;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerAdapter_HomeTimetable extends RecyclerView.Adapter<RecyclerAdapter_HomeTimetable.TimetableHolder> {

    Context context;
    ArrayList<HomeFragment.HomeTimetableItem> arrayList;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    Date currentTime;
    int flag=0;

    public RecyclerAdapter_HomeTimetable(Context context, ArrayList<HomeFragment.HomeTimetableItem> arrayList)
    {
        this.context=context;
        this.arrayList = arrayList;
        try {
            currentTime = dateFormat.parse(dateFormat.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public TimetableHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.home_timetable_card_item,parent,false);
        return new TimetableHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableHolder holder, int position) {

        holder.subject.setText(arrayList.get(position).subject);
        holder.time.setText(arrayList.get(position).time);
        String s = arrayList.get(position).time;
        try {
            Date time = dateFormat.parse(s.substring(s.length()-5,s.length()));
            if(time.compareTo(currentTime)<0) {
                holder.time.setTextColor(Color.parseColor("#FF3C3C3C"));
                holder.subject.setTextColor(Color.parseColor("#FF3C3C3C"));
            }
            else if(flag==0)
            {
                Date starttime = dateFormat.parse(s.substring(0,5));
                Log.i("yoyo5",dateFormat.format(starttime));

                if(starttime.compareTo(currentTime)>0)
                holder.subject.setText(arrayList.get(position).subject+" (Next)");
                else
                    holder.subject.setText(arrayList.get(position).subject+" (Ongoing)");
                flag = 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class TimetableHolder extends RecyclerView.ViewHolder {

        TextView subject,time;

        public TimetableHolder(@NonNull View itemView) {
            super(itemView);
            this.subject = itemView.findViewById(R.id.Subject);
            this.time = itemView.findViewById(R.id.Time);
        }
    }
}
