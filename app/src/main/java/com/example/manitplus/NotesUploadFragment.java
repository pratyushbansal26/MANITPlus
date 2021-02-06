package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

public class NotesUploadFragment extends Fragment {

    Button upload;
    EditText select_pdf;
    Spinner select_subject;
    StorageReference storageReference;
    Uri fileUri=null;
    int pages=0;
    String title;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_notes,container,false);

        select_pdf = view.findViewById(R.id.select_pdf);
        select_subject = (Spinner) view.findViewById(R.id.select_subject);
        upload = view.findViewById(R.id.Upload);


        Date today = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String day = df.format(today);

        //Fill Spinner
        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year)
                .child(MainActivity.Branch).child("Subjects");
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> mArrayList = new ArrayList<>();
                mArrayList.add("Choose Subject");
                for(DataSnapshot subs : snapshot.getChildren())
                {
                    String s = subs.getKey().toString();
                    mArrayList.add(s);
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item , mArrayList);
                //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                select_subject.setAdapter(spinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();

        //Choose File Clicked
        select_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPdfFile();
            }
        });

        //Upload Button Clicked
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_subject.getSelectedItem().toString().equals("Choose Subject"))
                    Toast.makeText(getContext(),"Please Select The Subject!",Toast.LENGTH_SHORT).show();
                else if(fileUri==null)
                    Toast.makeText(getContext(),"Please Select A File!",Toast.LENGTH_SHORT).show();
                else
                    uploadPdfFile(fileUri);
            }
        });

        return view;
    }

    //Select Pdf Function
    private void selectPdfFile() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent , "Select PDF file") , 1);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
            fileUri = data.getData();
    }

    //Upload Function
    private void uploadPdfFile(final Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading...");
        progressDialog.setMessage("0% Uploaded");

        String pdf_name = getFileName(data);
        title=pdf_name;

        title =title.replace('.','-');
        title =title.replace('/','-');
        title =title.replace('$','-');
        title =title.replace('#','-');
        title =title.replace('[','-');
        title =title.replace(']','-');

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Upload Notes");
        builder.setMessage("Are you sure you want to Upload this File?");
        builder.setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year).
                        child(MainActivity.Branch).child(MainActivity.Section).child("Notes").child(select_subject.getSelectedItem().toString())
                        .child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists())
                            Toast.makeText(getContext() , "File Name Already Exists" , Toast.LENGTH_SHORT).show();
                        else
                        {
                            progressDialog.show();
                            StorageReference reff1 = storageReference.child("Colleges").child("MANIT").child(MainActivity.Year).
                                    child(MainActivity.Branch).child(MainActivity.Section).child("Notes")
                                    .child(select_subject.getSelectedItem().toString()).child(pdf_name);
                            reff1.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            progressDialog.dismiss();
                                            Toast.makeText(getContext() , "File Uploaded Successfully" , Toast.LENGTH_SHORT).show();

                                            reff1.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                                @Override
                                                public void onSuccess(StorageMetadata storageMetadata) {

                                                    String day = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
                                                    String size;
                                                    if(taskSnapshot.getTotalByteCount() < 1024*1024)
                                                        size = taskSnapshot.getTotalByteCount()/1024 + " KB";
                                                    else
                                                        size = taskSnapshot.getTotalByteCount()/(1024*1024) + " MB";

                                                    File file = new File(getPath(data));
                                                    try(PDDocument pdDocument = PDDocument.load(file))
                                                    {
                                                        pages = pdDocument.getNumberOfPages();
                                                    }
                                                    catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    storageMetadata.getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            upload_pdf uploadpdf = new upload_pdf(MainActivity.Name , day ,pages, pdf_name ,size , task.getResult().toString());
                                                            FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child(MainActivity.Year).
                                                                    child(MainActivity.Branch).child(MainActivity.Section).child("Notes").child(select_subject.getSelectedItem().toString())
                                                                    .child(title).setValue(uploadpdf);
                                                            Toast.makeText(getContext() , "Success" , Toast.LENGTH_SHORT);
                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                    double progress = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                    progressDialog.setMessage((int) progress + "% Uploaded");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext() , "Unable to Upload" , Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    //Pdf Class
    public class upload_pdf {

        public String pdf_name , url , author , day , size;
        int pages;

        public upload_pdf() {
        }

        public upload_pdf(String author, String day, int pages ,String pdf_name , String size , String url) {
            this.pdf_name = pdf_name;
            this.author = author;
            this.pages = pages;
            this.day = day;
            this.size = size;
            this.url = url;
        }

        public String getPdf_name() {
            return pdf_name;
        }

        public String getUrl() {
            return url;
        }

        public String getAuthor() {
            return author;
        }

        public String getDay() {
            return day;
        }

        public String  getSize() {
            return size;
        }

        public int getPages() {
            return pages;
        }
    }

    public void onResume() {
        super.onResume();
        //To Set Edittext Text
        if(fileUri!=null)
            select_pdf.setText(getFileName(fileUri));
    }


    //Get File Path From Content URI
    private String getPath(final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if(isKitKat) {
            // MediaStore (and general)
            return getForApi19(uri);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    @TargetApi(19)
    private String getForApi19(Uri uri) {
        if (DocumentsContract.isDocumentUri(getActivity(), uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                Log.e("MANIT+", "+++ Downloads External Document URI");
                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    String[] contentUriPrefixesToTry = new String[]{
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads",
                            "content://downloads"
                    };
                    for (String contentUriPrefix : contentUriPrefixesToTry) {
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                        try {
                            String path = getDataColumn(contentUri, null, null);
                            Log.d("Path" , path);
                            if (path != null) {
                                return path;
                            }
                        } catch (Exception e) {}
                    }

                    InputStream in = null;
                    try {
                        in = getContext().getContentResolver().openInputStream(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    File file = new File(getContext().getCacheDir().getAbsolutePath()+"/"+id);
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while((len=in.read(buf))>0){
                            out.write(buf,0,len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            if ( out != null ) {
                                out.close();
                            }
                            in.close();
                        } catch ( IOException e ) {
                            e.printStackTrace();
                        }
                    }
                    return file.getAbsolutePath();

                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}