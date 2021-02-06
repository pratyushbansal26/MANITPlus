package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.annotation.TargetApi;
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
import android.widget.Button;
import android.widget.EditText;
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
import com.onesignal.OneSignal;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class NoticeUploadFragment extends Fragment {

    private Button upload;
    private EditText choosefile,description;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private StorageReference rootstorage = FirebaseStorage.getInstance().getReference();
    private Uri fileuri=null;
    private String filepath;
    private String filename;
    String title;
    static String type;
    private String uploaddate,sizeString;
    PDDocument pdDocument;
    private String tag = "TAG";
    private int pages;
    private Date counter;

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notice_upload,container,false);

        upload = view.findViewById(R.id.UploadButton);
        choosefile = view.findViewById(R.id.ChooseFile);
        description = view.findViewById(R.id.Description);

        PDFBoxResourceLoader.init(getContext());

        MainActivity.toolbar.setTitle("Notices");

        //Choose File
        choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent,1);
            }
        });


        //Upload File
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Checking All Fields
                if(fileuri!=null)
                {
                    filename = getFileName(fileuri);
                    title = getTitle(filename);
                }

                if(fileuri==null)
                    Toast.makeText(getContext(), "Please Select A File!", Toast.LENGTH_SHORT).show();
                else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Upload Assignment");
                        builder.setMessage("Are you sure you want to Upload this File?");
                        builder.setNegativeButton("CANCEL",null);

                        builder.setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                File file = new File(filepath);

                                try (PDDocument pdDocument = PDDocument.load(file)){
                                    pages = pdDocument.getNumberOfPages();
                                } catch (IOException e) {
                                    Log.i("M", e.toString());
                                }

                                root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Notices")
                                        .child(title).addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists())
                                            Toast.makeText(getContext(), "Please Change The Filename As It Already Exists In Database!!", Toast.LENGTH_SHORT).show();
                                        else
                                        {
                                            //Creating ProgressDialog For Upload
                                            final ProgressDialog progressDialog = new ProgressDialog(getContext());
                                            progressDialog.setCancelable(false);
                                            progressDialog.setTitle("Uploading...");
                                            progressDialog.setMessage("0% Uploaded");
                                            progressDialog.show();
                                            counter = new Date();

                                            final StorageReference pdfReference = rootstorage.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Notices")
                                                    .child(filename);

                                            pdfReference.putFile(fileuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                    progressDialog.dismiss();
                                                    Toast.makeText(getContext(), "Notice Uploaded Successfully!!", Toast.LENGTH_SHORT).show();

                                                    //Updating RealTime Database


                                                    pdfReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                                        @Override
                                                        public void onSuccess(StorageMetadata storageMetadata) {

                                                            long size = storageMetadata.getSizeBytes() / 1024;
                                                            if (size > 1024) {
                                                                size = size / 1024;
                                                                sizeString = "" + size + " MB";
                                                            } else
                                                                sizeString = "" + size + " KB";

                                                            long millis = storageMetadata.getCreationTimeMillis();
                                                            Date date = new Date(millis);
                                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                                            uploaddate = dateFormat.format(date);
                                                            final DatabaseReference newPdf = root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Notices")
                                                                    .child(title);

                                                            storageMetadata.getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {

                                                                    String url = task.getResult().toString();
                                                                    newPdf.child("URL").setValue(url);
                                                                    newPdf.child("Uploader").setValue(Name);
                                                                    newPdf.child("File Name").setValue(filename);
                                                                    newPdf.child("Size").setValue(sizeString);
                                                                    newPdf.child("Upload Date").setValue(uploaddate);
                                                                    newPdf.child("Pages").setValue(pages);
                                                                    newPdf.child("Description").setValue(description.getText().toString());
                                                                    if(description.getText().toString().equals(""))
                                                                        newPdf.child("Description").setValue("No Description Provided...");
                                                                    else
                                                                        newPdf.child("Description").setValue(description.getText().toString());
                                                                }
                                                            });
                                                        }
                                                    });

                                                    //Push Notification To Others
                                                    root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Player Ids").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                            //Decide The Users To Send Notification To
                                                            JSONObject body = new JSONObject();
                                                            try {
                                                                body.put("headings", new JSONObject().put("en", "New Notice"));
                                                                body.put("large_icon", R.drawable.ic_notification_notice);
                                                                body.put("contents", new JSONObject().put("en", "" + filename));

                                                                final JSONArray jsonArray = new JSONArray();

                                                                for (DataSnapshot user : dataSnapshot.getChildren()) {
                                                                    if(!user.getValue().toString().equals(MainActivity.PlayerId))
                                                                        jsonArray.put(user.getValue().toString());
                                                                }

                                                                jsonArray.put(MainActivity.PlayerId);
                                                                body.put("include_player_ids", jsonArray);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                            //Post Notifications..
                                                            OneSignal.postNotification(body, new OneSignal.PostNotificationResponseHandler() {
                                                                @Override
                                                                public void onSuccess(JSONObject response) {
                                                                    Log.i("Hello", "Notification Sent Successfully..");
                                                                }

                                                                @Override
                                                                public void onFailure(JSONObject response) {
                                                                    Log.i("Hello", response.toString());
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(), "An Error Occured!", Toast.LENGTH_SHORT).show();

                                                }
                                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                                                    //Updating Progress Every .5 Secs
                                                    Date currdate = new Date();
                                                    if (currdate.getTime() - counter.getTime() >= 500) {
                                                        counter = currdate;
                                                        int percentage = (int) (((double) taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100);
                                                        progressDialog.setMessage(percentage + "% Uploaded");
                                                    }
                                                }
                                            });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                        builder.show();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            fileuri = data.getData();
            filepath = getPath(fileuri);
        }
    }

    public void onResume () {
        super.onResume();
        //To Set Edittext Text
        if(fileuri!=null)
            choosefile.setText(getFileName(fileuri));
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
        Log.e(tag, "+++ API 19 URI :: " + uri);
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            Log.e(tag, "+++ Document URI");
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                Log.e(tag, "+++ External Document URI");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Log.e(tag, "+++ Primary External Document URI");
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
                Log.e(tag, "+++ Media Document URI");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    Log.e(tag, "+++ Image Media Document URI");
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    Log.e(tag, "+++ Video Media Document URI");
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    Log.e(tag, "+++ Audio Media Document URI");
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.e(tag, "+++ No DOCUMENT URI :: CONTENT ");

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.e(tag, "+++ No DOCUMENT URI :: FILE ");
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
