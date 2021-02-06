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
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.manitplus.MainActivity.Admin;
import static com.example.manitplus.MainActivity.Branch;
import static com.example.manitplus.MainActivity.Name;
import static com.example.manitplus.MainActivity.ScholarNo;
import static com.example.manitplus.MainActivity.Section;
import static com.example.manitplus.MainActivity.Year;

public class RecyclerAdapter_Assignment_Questions extends RecyclerView.Adapter<RecyclerAdapter_Assignment_Questions.PdfHolder> {

    Context context;
    ArrayList<AssignmentFragment.Pdf> arrayList;
    Date counter;
    int notificationId=1;

    RecyclerAdapter_Assignment_Questions(Context context, ArrayList<AssignmentFragment.Pdf> arrayList)
    {
        this.context=context;
        this.arrayList=arrayList;
    }

    @NonNull
    @Override
    public RecyclerAdapter_Assignment_Questions.PdfHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_assignment_question,parent,false);
        return new PdfHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerAdapter_Assignment_Questions.PdfHolder holder, final int position) {

        holder.name.setText(arrayList.get(position).name);
        holder.uploader.setText(arrayList.get(position).uploader);
        holder.uploaddate.setText(arrayList.get(position).uploaddate);
        holder.submissiondate.setText(arrayList.get(position).submissiondate);
        holder.subject.setText(arrayList.get(position).subject);
        holder.pages.setText(arrayList.get(position).pages);
        holder.size.setText(arrayList.get(position).size);
        if(arrayList.get(position).completed)
            holder.marker.setBackgroundResource(R.drawable.ic_check_circle);

        //Change Card Background to Faded
        try {
            if(ChangeCardViewBackground(arrayList.get(position).submissiondate))
            {
                holder.cardConstraint.setBackgroundResource(R.drawable.card_background4);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        File path = new File(Environment.getExternalStorageDirectory()+"/MANIT+/Assignments/Questions/"+arrayList.get(position).subject,arrayList.get(position).name);
        if(path.exists())
            holder.download.setBackgroundResource(R.drawable.ic_check);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final File file = new File(Environment.getExternalStorageDirectory()+"/MANIT+/Assignments/Questions/"+arrayList.get(position).subject,
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
                    intent.setDataAndType(Contenturi,getFileType(file) );
                    v.getContext().startActivity(intent);
                }
            }
        });

        //Popup Menu Click
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(context,holder.options);

                popupMenu.inflate(R.menu.popup_menu);
                if(!arrayList.get(position).uploader.equals(Name) && !Admin)
                    popupMenu.getMenu().removeItem(R.id.Delete);

                if(arrayList.get(position).completed)
                    popupMenu.getMenu().getItem(0).setTitle("Mark As Pending");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        final DatabaseReference pdf = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(Year).child(Branch)
                                .child(Section).child("Assignments").child("Questions").child(arrayList.get(position).subject)
                                .child(getTitle(arrayList.get(position).name));


                        if(item.getItemId()==R.id.Delete)
                        {
                            //Alert Dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Delete File");
                            builder.setMessage("Are you sure to Delete this Assignment?");
                            builder.setNegativeButton("CANCEL",null);
                            builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Delete From Firebase Storage
                                    FirebaseStorage.getInstance().getReferenceFromUrl(arrayList.get(position).url).delete();

                                    //Delete From Firebase Database
                                    pdf.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context,"The File was Successfully Deleted!",Toast.LENGTH_SHORT).show();
                                            arrayList.remove(position);
                                            notifyItemRemoved(position);
                                        }
                                    });
                                }
                            });
                            builder.show();
                            return false;
                        }
                        else
                        {
                            if(item.getTitle().equals("Mark As Pending"))
                            {
                                pdf.child("Users").child(ScholarNo).removeValue();
                                holder.marker.setBackgroundResource(R.drawable.ic_create);
                                arrayList.get(position).completed = false;
                            }
                            else
                            {
                                pdf.child("Users").child(ScholarNo).setValue(1);
                                holder.marker.setBackgroundResource(R.drawable.ic_check_circle);
                                arrayList.get(position).completed = true;
                            }
                        }
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

    public class PdfHolder extends RecyclerView.ViewHolder {

        public TextView subject,name,submissiondate,pages,uploader,uploaddate,size,download,marker;
        Button options;
        View itemView;
        ConstraintLayout cardConstraint;

        public PdfHolder(@NonNull View itemView) {
            super(itemView);

            subject=itemView.findViewById(R.id.Subject);
            name=itemView.findViewById(R.id.Name);
            submissiondate=itemView.findViewById(R.id.SubmissionDate);
            pages=itemView.findViewById(R.id.Pages);
            uploaddate=itemView.findViewById(R.id.UploadDate);
            uploader=itemView.findViewById(R.id.Uploader);
            size= itemView.findViewById(R.id.Size);
            download= itemView.findViewById(R.id.Download);
            options = itemView.findViewById(R.id.Options);
            cardConstraint = itemView.findViewById(R.id.CardConstraint);
            marker = itemView.findViewById(R.id.Marker);
            this.itemView = itemView;
        }
    }

    boolean ChangeCardViewBackground(String dateString) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date today1 = new Date();
        String s = dateFormat.format(today1);

        Date today = dateFormat.parse(s);
        if(dateFormat.parse(dateString).compareTo(today)<0)
            return true;
        return false;
    }
    String getTitle(String pdf_name)
    {
        String title=pdf_name;

        title =title.replace('.','-');
        title =title.replace('/','-');
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
