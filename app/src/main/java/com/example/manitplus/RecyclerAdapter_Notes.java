package com.example.manitplus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static com.example.manitplus.MainActivity.Admin;

public class RecyclerAdapter_Notes extends RecyclerView.Adapter<RecyclerAdapter_Notes.ViewHolderNotes> {

    Context context;
    ArrayList<NotesFragment.Model2> mArrayList;
    long total;
    Date counter;
    int notificationId = 1;

    public RecyclerAdapter_Notes(Context ct, ArrayList<NotesFragment.Model2> arrayList){
        this.context= ct;
        mArrayList = arrayList;
    }

    @NonNull
    @Override
    public RecyclerAdapter_Notes.ViewHolderNotes onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.recycler_item_notes,parent,false);
        return new ViewHolderNotes(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerAdapter_Notes.ViewHolderNotes holder, final int position) {


        final DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Colleges").
                child("MANIT").child(MainActivity.Year).child(MainActivity.Branch).
                child(MainActivity.Section).child("Notes").child(mArrayList.get(position).subject_name).
                child(getTitle(mArrayList.get(position).pdf_name)).child("Likes").child(MainActivity.ScholarNo);
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    holder.like_button.setBackgroundResource(R.drawable.ic_thumb_up_selected);
                }
                else {
                    holder.like_button.setBackgroundResource(R.drawable.ic_thumb_up);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if((mArrayList.get(position).author).equals(MainActivity.Name) || Admin) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.delete.setEnabled(true);
        }
        else {
            holder.delete.setVisibility(View.INVISIBLE);
            holder.delete.setEnabled(false);
        }

        //Likes Count
        FirebaseDatabase.getInstance().getReference().child("Colleges").
                child("MANIT").child(MainActivity.Year).child(MainActivity.Branch).
                child(MainActivity.Section).child("Notes").child(mArrayList.get(position).subject_name).
                child(getTitle(mArrayList.get(position).pdf_name)).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.likes.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Like Button Clicked
        holder.like_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                reff.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            holder.like_button.setBackgroundResource(R.drawable.ic_thumb_up);
                            reff.removeValue();
                        }
                        else
                        {
                            holder.like_button.setBackgroundResource(R.drawable.ic_thumb_up_selected);
                            reff.setValue(1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        //Delete Clicked
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context,holder.delete);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.getMenu().removeItem(R.id.Mark);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete File");
                        builder.setMessage("Are you sure you want to Delete this File?");

                        // add the buttons
                        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(mArrayList.get(position).url);
                                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                FirebaseDatabase.getInstance().getReference().child("Colleges").
                                        child("MANIT").child(MainActivity.Year).child(MainActivity.Branch).
                                        child(MainActivity.Section).child("Notes").child(mArrayList.get(position).subject_name).
                                        child(getTitle(mArrayList.get(position).pdf_name)).removeValue();

                                mArrayList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mArrayList.size());

                            }
                        });
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        // create and show the alert dialog
                        builder.create().show();

                        return false;
                    }
                });
                popupMenu.show();
            }
        });


        //File Clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File pathInMobile,localFile;
                pathInMobile = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Notes/" +
                        mArrayList.get(position).subject_name);
                localFile = new File(pathInMobile, mArrayList.get(position).pdf_name);

                if(localFile.exists())
                {
                    //Open File
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri Contenturi = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.manitplus.fileprovider", localFile);
                    intent.setDataAndType(Contenturi,getFileType(localFile));
                    context.startActivity(intent);
                }
                else
                    Toast.makeText(context,"Please Download The File To Open...",Toast.LENGTH_SHORT).show();
            }
        });

        //Download Button Clicked
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                File pathInMobile , localFile;
                try {
                    pathInMobile = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Notes/" +
                            mArrayList.get(position).subject_name);
                    localFile = new File(pathInMobile, mArrayList.get(position).pdf_name);

                    if (localFile.exists())
                        Toast.makeText(v.getContext(), "File Already Downloaded", Toast.LENGTH_SHORT).show();
                        else {


                        //Creating Notification
                        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"MyNotifications");
                        builder.setContentTitle(mArrayList.get(position).pdf_name);
                        builder.setContentText("Starting Download");
                        builder.setProgress(0,0,true);
                        builder.setSmallIcon(R.drawable.ic_file_download);
                        builder.setOnlyAlertOnce(true);
                        counter = new Date();

                        //Show Notification
                        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                        notificationManager.notify(notificationId,builder.build());

                        FirebaseStorage.getInstance().getReferenceFromUrl(mArrayList.get(position).url).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {


                                //Download Complete Message
                                builder.setProgress(0,0,false);
                                builder.setContentText("Download Complete");
                                builder.setSmallIcon(R.drawable.ic_check);
                                notificationManager.notify(notificationId,builder.build());
                                notificationId++;
                                holder.download.setBackgroundResource(R.drawable.ic_check);

                                //Open File
                                final Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                Uri Contenturi = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.manitplus.fileprovider", localFile);
                                intent.setDataAndType(Contenturi,getFileType(localFile));
                                context.startActivity(intent);

                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {

                                //Updating Progress Every .5 Secs
                                Date date = new Date();
                                double percentage = (double)snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                                builder.setProgress(100,(int)(percentage*100),false);
                                builder.setContentText(""+(int)(percentage*100)+"% Downloaded");
                                if(date.getTime()-counter.getTime()>=500) {
                                    notificationManager.notify(notificationId, builder.build());
                                    counter = date;
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                                notificationManager.cancel(notificationId);
                            }
                        });
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //get from firebase

        holder.subject_name.setText(mArrayList.get(position).subject_name);
        holder.pdf_name.setText(mArrayList.get(position).pdf_name);
        holder.author.setText("Uploaded By : " + mArrayList.get(position).author);
        holder.pages.setText("Pages : " + String.valueOf( mArrayList.get(position).pages));
        holder.size.setText("(" + (mArrayList.get(position).size)+")");
        holder.day.setText(mArrayList.get(position).day);
        holder.download.setBackgroundResource(R.drawable.ic_file_download);
        holder.likes.setText(String.valueOf(mArrayList.get(position).likes));

        File pathInMobile , localFile;
        pathInMobile = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Notes/" +
                mArrayList.get(position).subject_name);
        localFile = new File(pathInMobile, mArrayList.get(position).pdf_name);
        if(!localFile.exists()) {
            holder.download.setBackgroundResource(R.drawable.ic_file_download);
        }
        else {
            holder.download.setBackgroundResource(R.drawable.ic_check);
        }

    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public class ViewHolderNotes extends RecyclerView.ViewHolder{

        TextView pdf_name;
        TextView subject_name;
        TextView author;
        TextView day;
        TextView pages;
        TextView size;
        TextView likes;
        Button like_button;
        Button download;
        Button delete;
        View itemView;

        public ViewHolderNotes(@NonNull View itemView) {
            super(itemView);

            pdf_name = (TextView) itemView.findViewById(R.id.pdf_name);
            subject_name = (TextView) itemView.findViewById(R.id.subject_name);
            author = (TextView) itemView.findViewById(R.id.author);
            day = (TextView) itemView.findViewById(R.id.day);
            pages = (TextView) itemView.findViewById(R.id.pages);
            size = (TextView) itemView.findViewById(R.id.size);
            download = (Button) itemView.findViewById(R.id.download);
            like_button = (Button) itemView.findViewById(R.id.like_button);
            likes = (TextView) itemView.findViewById(R.id.likes);
            delete = (Button) itemView.findViewById(R.id.delete);
            this.itemView = itemView;

        }
    }

    private String getTitle(String pdf_name)
    {
        String title=pdf_name;

        title =title.replace('.','-');
        title =title.replace('/','-');
        title =title.replace('$','-');
        title =title.replace('#','-');
        title =title.replace('[','-');
        title =title.replace(']','-');
        return title;
    }

    private String getFileType(File localFile)
    {
        if(localFile.getName().endsWith(".pdf"))
            return  "application/pdf";
        else if(localFile.getName().endsWith(".docx") || localFile.getName().endsWith(".doc"))
            return "application/msword";
        else if(localFile.getName().endsWith(".ppt") || localFile.getName().endsWith(".pptx"))
            return "application/vnd.ms-powerpoint";
        else if(localFile.getName().endsWith(".xls") || localFile.getName().endsWith(".xlsx"))
            return "application/vnd.ms-excel";
        else if(localFile.getName().endsWith(".rar"))
            return "application/x-rar-compressed";
        else if(localFile.getName().endsWith(".zip"))
            return "application/zip";
        else if(localFile.getName().endsWith(".jpg") || localFile.getName().endsWith(".jpeg") || localFile.getName().endsWith(".png"))
            return "image/jpeg";
        else if(localFile.getName().endsWith(".txt"))
            return "text/plain";
        else
            return "*/*";
    }

}