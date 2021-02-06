package com.example.manitplus;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.manitplus.MainActivity.Admin;

public class AnnouncementUploadFragment extends Fragment {

    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;
    String ScholarNo = MainActivity.ScholarNo;
    String Name = MainActivity.Name;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();

    EditText title,message;
    Button post;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_announcement_upload,container,false);


        title = view.findViewById(R.id.Title);
        message = view.findViewById(R.id.Message);
        post = view.findViewById(R.id.Post);

        MainActivity.toolbar.setTitle("Announcements");

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.getText().toString().equals(""))
                    Toast.makeText(getContext(),"Please Enter The Title",Toast.LENGTH_SHORT).show();

                else if(message.getText().toString().equals(""))
                Toast.makeText(getContext(),"Please Enter The Message",Toast.LENGTH_SHORT).show();

                else if(Admin)
                {
                    final DatabaseReference Announcement = root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Announcements");
                            Announcement.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child(getTitle(title.getText().toString())).exists())
                                Toast.makeText(getContext(),"Title Already Exists!",Toast.LENGTH_SHORT).show();
                            else
                            {
                                Date today = new Date();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                Announcement.child(getTitle(title.getText().toString())).child("Title").setValue(title.getText().toString());
                                Announcement.child(getTitle(title.getText().toString())).child("Message").setValue(message.getText().toString());
                                Announcement.child(getTitle(title.getText().toString())).child("Upload Date").setValue(dateFormat.format(today));
                                Announcement.child(getTitle(title.getText().toString())).child("From").setValue(Name);


                                //Push Notification To Others
                                root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Player Ids").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        //Decide The Users To Send Notification To
                                        JSONObject notification = new JSONObject();
                                        try {
                                            notification.put("headings", new JSONObject().put("en", "New Announcement"));
                                            notification.put("large_icon", R.drawable.ic_notification_announcement);
                                            notification.put("contents", new JSONObject().put("en", title.getText().toString()));

                                            final JSONArray recpients = new JSONArray();

                                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                                recpients.put(user.getValue().toString());
                                            }

                                            notification.put("include_player_ids", recpients);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        //Post Notifications..
                                        OneSignal.postNotification(notification, new OneSignal.PostNotificationResponseHandler() {
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


                                Toast.makeText(getContext(),"Posted!",Toast.LENGTH_SHORT).show();

                                //Get Back To Announcements Fragment
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container,new AnnouncementsFragment()).commit();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                    Toast.makeText(getContext(),"Action Not Allowed!!\n(You Are Not An Admin)",Toast.LENGTH_SHORT).show();
            }
        });

        return  view;
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
