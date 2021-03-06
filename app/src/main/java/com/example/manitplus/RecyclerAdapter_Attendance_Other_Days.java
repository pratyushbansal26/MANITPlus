package com.example.manitplus;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class RecyclerAdapter_Attendance_Other_Days extends RecyclerView.Adapter<RecyclerAdapter_Attendance_Other_Days.PeriodViewHolder> {

    Context context;
    ArrayList<AttendanceFragment.Period_Item> arrayList;

    RecyclerAdapter_Attendance_Other_Days(Context context, ArrayList<AttendanceFragment.Period_Item> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;
        if(arrayList.size()==0)
            AttendanceFragment.holiday.setText("No Classes Today!! \uD83D\uDE09");
    }

    @NonNull
    @Override
    public PeriodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_other_days,parent,false);
        return new PeriodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PeriodViewHolder holder, final int position) {


        int present = arrayList.get(position).present;
        int absent = arrayList.get(position).absent;
        int total = present+absent;
        final ArrayList<Date> dateArrayList=arrayList.get(position).dateArrayList;
        HashMap<Date,Integer> dateHashMap=arrayList.get(position).dateHashMap;

        holder.subject.setText(arrayList.get(position).sub);
        holder.time.setText(arrayList.get(position).time);
        float percentage = (float) present/(float) (present+absent);
        holder.percentage.setText(String.format("%.1f",percentage*100)+"%");
        holder.count.setText(Integer.toString(present)+"/"+Integer.toString(present+absent));

        if(percentage<0.75) {
            Drawable drawable = ContextCompat.getDrawable(context,R.drawable.progress_drawable_red);
            holder.progressBar.setProgressDrawable(drawable);
            if((int)(percentage*100)==70)
                holder.progressBar.setProgress(71);
        }
        holder.progressBar.setProgress((int)(percentage*100));

        if((float)present/(total)<0.75)
        {
            int c=0;
            while((float)(present+c)/(total+c)<0.75)
                c++;
            if(c==1)
                holder.status.setText("You have to Attend the Next 1 Class");
            else
                holder.status.setText("You have to Attend the Next "+Integer.toString(c)+" Classes");
        }
        else
        {
            int c=0;
            while((float)(present)/(total+c)>=0.75)
                c++;
            c--;
            if(c<=0)
                holder.status.setText("You can't Miss the Next Class");
            else if(c==1)
                holder.status.setText("You can Miss the Next 1 Class");
            else
                holder.status.setText("You can Miss the Next "+Integer.toString(c)+" Classes");
        }

        if(0<dateArrayList.size())
        {
            if(dateHashMap.get(dateArrayList.get(0))==1)
                holder.dot1.setBackgroundResource(R.drawable.green);
            else if(dateHashMap.get(dateArrayList.get(0))==2)
                holder.dot1.setBackgroundResource(R.drawable.red);
        }
        if(1<dateArrayList.size())
        {
            if(dateHashMap.get(dateArrayList.get(1))==1)
                holder.dot2.setBackgroundResource(R.drawable.green);
            else if(dateHashMap.get(dateArrayList.get(1))==2)
                holder.dot2.setBackgroundResource(R.drawable.red);
        }
        if(2<dateArrayList.size())
        {
            if(dateHashMap.get(dateArrayList.get(2))==1)
                holder.dot3.setBackgroundResource(R.drawable.green);
            else if(dateHashMap.get(dateArrayList.get(2))==2)
                holder.dot3.setBackgroundResource(R.drawable.red);
        }
        if(3<dateArrayList.size())
        {
            if(dateHashMap.get(dateArrayList.get(3))==1)
                holder.dot4.setBackgroundResource(R.drawable.green);
            else if(dateHashMap.get(dateArrayList.get(3))==2)
                holder.dot4.setBackgroundResource(R.drawable.red);
        }
        if(4<dateArrayList.size())
        {
            if(dateHashMap.get(dateArrayList.get(4))==1)
                holder.dot5.setBackgroundResource(R.drawable.green);
            else if(dateHashMap.get(dateArrayList.get(4))==2)
                holder.dot5.setBackgroundResource(R.drawable.red);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttendanceCalenderFragment.subject = arrayList.get(position).sub;
                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AttendanceCalenderFragment()).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class PeriodViewHolder extends RecyclerView.ViewHolder {

        TextView subject;
        TextView percentage;
        TextView status;
        TextView time;
        TextView count;
        TextView dot1;
        TextView dot2;
        TextView dot3;
        TextView dot4;
        TextView dot5;
        ProgressBar progressBar;
        View itemView;

        public PeriodViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.Subject);
            status = itemView.findViewById(R.id.Status);
            count = itemView.findViewById(R.id.Count);
            time = itemView.findViewById(R.id.Time);
            progressBar = itemView.findViewById(R.id.Progress);
            percentage = itemView.findViewById(R.id.ProgressPercentage);
            dot1 = itemView.findViewById(R.id.Dot1);
            dot2 = itemView.findViewById(R.id.Dot2);
            dot3 = itemView.findViewById(R.id.Dot3);
            dot4 = itemView.findViewById(R.id.Dot4);
            dot5 = itemView.findViewById(R.id.Dot5);
            this.itemView = itemView;
        }
    }
}
