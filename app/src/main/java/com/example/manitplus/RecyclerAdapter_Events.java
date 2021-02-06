package com.example.manitplus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerAdapter_Events extends RecyclerView.Adapter<RecyclerAdapter_Events.ProgrammingViewHolder>{

    private static final String TAG = "RecyclerAdapter_Events";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference users = db.getReference().child("Colleges").child("MANIT").child("Events");


    ArrayList<EventClass> mList;
    Context context;

    public RecyclerAdapter_Events(Context context, ArrayList<EventClass> mList){
        this.mList = mList;
        this.context = context;

    }

    @NonNull
    @Override


    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_event,parent, false);

        return new ProgrammingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {


        EventClass odel = mList.get(position);
        holder.txtTitle.setText(odel.getTitle());
        holder.txtDate.setText(odel.getDate());
        holder.txtSocietyName.setText(odel.getSocietyname());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String s = dateFormat.format(new Date());
        try {
            Date today = dateFormat.parse(s);
            if(dateFormat.parse(mList.get(position).getDate()).compareTo(today)<0)
                holder.constraintLayout.setBackgroundResource(R.drawable.card_background14_faded);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(getTitle(odel.getTitle())).child("Likes").child(MainActivity.ScholarNo).exists()){
                    holder.likebutton.setBackgroundResource(R.drawable.ic_favorite);

                }else{
                    holder.likebutton.setBackgroundResource(R.drawable.ic_favorite_border);

                }
                holder.likecount.setText(String.valueOf(snapshot.child(getTitle(odel.getTitle())).child("Likes").getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Likes Count
        db.getReference().child("Colleges").child("MANIT").child("Events").child(getTitle(odel.getTitle())).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.likecount.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Like Clicked
        holder.likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                users = db.getReference().child("Colleges").child("MANIT").child("Events").child(getTitle(odel.getTitle())).child("Likes");
                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(MainActivity.ScholarNo).exists()){
                            holder.likebutton.setBackgroundResource(R.drawable.ic_favorite_border);
                            db.getReference().child("Colleges").child("MANIT").child("Events").child(getTitle(odel.getTitle())).child("Likes").child(MainActivity.ScholarNo).removeValue();
                        }
                        else{
                            holder.likebutton.setBackgroundResource(R.drawable.ic_favorite);
                            db.getReference().child("Colleges").child("MANIT").child("Events").child(getTitle(odel.getTitle())).child("Likes").child(MainActivity.ScholarNo).setValue("1");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //Item Clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDescriptionFragment.event = mList.get(position);
                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new EventDescriptionFragment()).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ProgrammingViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        TextView txtTitle;
        TextView txtSocietyName;
        Button likebutton;
        TextView likecount;
        ConstraintLayout constraintLayout;
        View itemView;

        public ProgrammingViewHolder(@NonNull View itemView)
        {
            super(itemView);
            txtSocietyName = itemView.findViewById(R.id.txtSocietyName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            likebutton = itemView.findViewById(R.id.likebutton);
            likecount = itemView.findViewById(R.id.likecount);
            constraintLayout = itemView.findViewById(R.id.EventConstraint);
            this.itemView = itemView;
        }
    }


    private String getTitle(String filename)
    {
        String title;
        title = filename;
        title =title.replace('.','-');
        title =title.replace('/','-');
        title =title.replace('$','-');
        title =title.replace('#','-');
        title =title.replace('[','-');
        title =title.replace(']','-');

        return title;
    }
}