package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.InputStream;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class EventUploadFragment extends Fragment {
    private static final String TAG ="EventUploadFragment" ;
    EditText editDate,editTitle,editDescription,editurl,editScoietyname;
    Button browsebutton,uploadbutton;
    Bitmap bitmap;
    Uri filepath;
    ImageView posterimage;
    DatePickerDialog.OnDateSetListener setListener;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child("Colleges").child("MANIT").child("Events");


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_upload,container,false);

        editTitle = view.findViewById(R.id.editTitle);
        editDescription = view.findViewById(R.id.editDescription);
        editurl = view.findViewById(R.id.editurl);
        editScoietyname = view.findViewById(R.id.editSocietyname);

        editDate = view.findViewById(R.id.ed_date);
        browsebutton = view.findViewById(R.id.browsebutton);
        posterimage = view.findViewById(R.id.posterview);
        uploadbutton = view.findViewById(R.id.uploadview);


        //Choose File
        browsebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Please Select Image"),1);
            }
        });

        //Upload Clicked
        uploadbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Confirmation Dialog Box
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Upload Event");
                builder.setMessage("Please make sure all the filled Details are Correct.\n" +
                        "The only way to delete an uploaded Event before its Date is through contacting The Developers directly.");
                builder.setNegativeButton("CANCEL",null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadtofirebase();
                    }
                });
                builder.show();
            }
        });



        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month +1;
                        String date;
                        if(month<10){
                            if(day<10){
                                date = "0"+day+"/0"+month+"/"+year;
                            }else{
                                date = day + "/0" + month + "/" + year;
                            }
                        }else {
                            if(day<10){
                                date = "0"+day+"/"+month+"/"+year;
                            }else {
                                date = day + "/" + month + "/" + year;
                            }
                        }
                        editDate.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });

        return view;
    }

    private void uploadtofirebase() {
        if(filepath==null || editTitle.getText().toString().isEmpty()||editDate.getText().toString().isEmpty()||editScoietyname.getText().toString().isEmpty()){
            Toast.makeText(getContext(), "Please Fill All The Required Fields", Toast.LENGTH_SHORT).show();
        }else {
            ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setTitle("Uploading...");
            dialog.show();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference uploader = storage.getReference().child("Colleges").child("MANIT").child("Events").child(getTitle(getFileName(filepath)));
            Log.d(TAG, "uploadtofirebase: " + getFileName(filepath));
            uploader.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    if (editurl.getText().toString().isEmpty()) {
                                        editurl.setText("Registration Link Not Available");
                                    }
                                    if (editDescription.getText().toString().isEmpty()) {
                                        editDescription.setText("Description Not Available");
                                    }

                                    EventClass mmodel = new EventClass(uri.toString(), editTitle.getText().toString(), editDescription.getText().toString(), editDate.getText().toString(), editScoietyname.getText().toString(), editurl.getText().toString());

                                    myRef.child(getTitle(editTitle.getText().toString())).setValue(mmodel);
                                }
                            });
                            Toast.makeText(getContext(), "Event Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            dialog.setMessage("" + (int) percent + "% Uploaded");
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            filepath = data.getData();
            try{
                InputStream inputStream = getActivity().getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                posterimage.setImageBitmap(bitmap);
            }catch(Exception ex){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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