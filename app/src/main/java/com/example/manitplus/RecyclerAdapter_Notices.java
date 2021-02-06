package com.example.manitplus;

import android.app.AlertDialog;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static com.example.manitplus.MainActivity.Admin;

public class RecyclerAdapter_Notices extends RecyclerView.Adapter<RecyclerAdapter_Notices.NoticeHolder> {

    Context context;
    ArrayList<NoticesFragment.Notice> arrayList;
    Date counter;
    int notificationId=1;

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;

    DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Notices");


    RecyclerAdapter_Notices(Context context, ArrayList<NoticesFragment.Notice> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;
    }

    @NonNull
    @Override
    public RecyclerAdapter_Notices.NoticeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_notice,parent,false);
        return new NoticeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerAdapter_Notices.NoticeHolder holder, final int position) {

        holder.name.setText(arrayList.get(position).name);
        holder.uploader.setText(arrayList.get(position).uploader);
        holder.uploaddate.setText(arrayList.get(position).uploaddate);
        holder.pages.setText(arrayList.get(position).pages);
        holder.size.setText(arrayList.get(position).size);
        holder.description.setText(arrayList.get(position).description);



        File path = new File(Environment.getExternalStorageDirectory()+"/College App/Notices",arrayList.get(position).name);

        if(path.exists())
            holder.download.setBackgroundResource(R.drawable.ic_check);

        //File Clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final File file = new File(Environment.getExternalStorageDirectory()+"/College App/Notices/",
                        arrayList.get(position).name);

                if(!file.exists()) {

                    //Downloading The File

                    //Creating Notification
                    final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"MyNotifications");
                    builder.setContentTitle(arrayList.get(position).name);
                    builder.setContentText("Starting Download");
                    builder.setProgress(0,0,true);
                    builder.setSmallIcon(R.drawable.ic_file_download);
                    builder.setOnlyAlertOnce(true);
                    counter = new Date();

                    //Show Notification
                    final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(notificationId,builder.build());

                    FirebaseStorage.getInstance().getReferenceFromUrl(arrayList.get(position).url).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //Toast.makeText(context.getApplicationContext(), "File Successfully Downloaded!", Toast.LENGTH_SHORT).show();

                            //Download Complete Message
                            builder.setProgress(0,0,false);
                            builder.setContentText("Download Complete");
                            builder.setSmallIcon(R.drawable.ic_check);
                            notificationManager.notify(notificationId,builder.build());
                            notificationId++;

                            //Opening The File
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri Contenturi = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.manitplus.fileprovider", file);
                            intent.setDataAndType(Contenturi, getFileType(file));
                            v.getContext().startActivity(intent);
                            holder.download.setBackgroundResource(R.drawable.ic_check);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context.getApplicationContext(), "An Error Occured!", Toast.LENGTH_SHORT).show();
                            notificationManager.cancel(notificationId);

                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            //Updating Progress Every .5 Secs
                            Date date = new Date();
                            double percentage = (double)taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();
                            builder.setProgress(100,(int)(percentage*100),false);
                            builder.setContentText(""+(int)(percentage*100)+"% Downloaded");
                            if(date.getTime()-counter.getTime()>=500) {
                                notificationManager.notify(notificationId, builder.build());
                                counter = date;
                            }
                        }
                    });
                }
                else {
                    //Directly Open The File
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri Contenturi = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.manitplus.fileprovider", file);
                    intent.setDataAndType(Contenturi, getFileType(file));
                    v.getContext().startActivity(intent);
                }
            }
        });


        //Popup Menu
        if(!arrayList.get(position).uploader.equals(Name) && !Admin)
        {
            holder.options.setEnabled(false);
            holder.options.setVisibility(View.INVISIBLE);
        }
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,holder.options);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.getMenu().removeItem(R.id.Mark);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete Notice");
                        builder.setMessage("Are you sure to Delete this Notice?");
                        builder.setNegativeButton("CANCEL",null);
                        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                root.child(getTitle(arrayList.get(position).name)).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        arrayList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context,"The Notice was Successfully Removed!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        builder.show();
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class NoticeHolder extends RecyclerView.ViewHolder {

        public TextView name,description,pages,uploader,uploaddate,size,download;
        Button options;
        View itemView;

        public NoticeHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.Name);
            description = itemView.findViewById(R.id.Description);
            pages=itemView.findViewById(R.id.Pages);
            uploaddate=itemView.findViewById(R.id.UploadDate);
            uploader=itemView.findViewById(R.id.Uploader);
            size= itemView.findViewById(R.id.Size);
            download= itemView.findViewById(R.id.Download);
            this.options= itemView.findViewById(R.id.Options);
            this.itemView = itemView;
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
