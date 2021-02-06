package com.example.manitplus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

import static com.example.manitplus.MainActivity.Admin;


public class RecyclerAdapter_Announcements extends RecyclerView.Adapter<RecyclerAdapter_Announcements.AnnouncementViewHolder> {

    Context context;
    ArrayList<AnnouncementsFragment.AnnouncementItem> arrayList;
    DatabaseReference admin = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Admins");

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    RecyclerAdapter_Announcements(Context context, ArrayList<AnnouncementsFragment.AnnouncementItem> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;

    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_announcement,parent,false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AnnouncementViewHolder holder, final int position) {

        holder.title.setText(arrayList.get(position).title);
        holder.message.setText(arrayList.get(position).message);
        holder.uploaddate.setText(arrayList.get(position).uploaddate);
        holder.from.setText("From - "+arrayList.get(position).from);

        if(!Admin)
        {
            holder.delete.setEnabled(false);
            holder.delete.setVisibility(View.INVISIBLE);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Announcement");
                builder.setMessage("Are you sure to Delete This Announcement?");
                builder.setNegativeButton("CANCEL", null);
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Announcements")
                                .child(getTitle(arrayList.get(position).title)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                arrayList.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        TextView title,message,uploaddate,from;
        View delete;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title=itemView.findViewById(R.id.Title);
            this.message=itemView.findViewById(R.id.Message);
            this.uploaddate=itemView.findViewById(R.id.UploadDate);
            this.from = itemView.findViewById(R.id.From);
            delete = itemView.findViewById(R.id.Delete);
        }
    }

    private String getTitle(String filename)
    {
        String title = filename;
        title =title.replace('.','-');
        title =title.replace('/','-');
        title =title.replace('$','-');
        title =title.replace('#','-');
        title =title.replace('[','-');
        title =title.replace(']','-');

        return title;
    }
}
