package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class AssignmentFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton questionsbutton,solutionsbutton;
    private ProgressBar loading;
    private SwipeRefreshLayout swipeRefreshLayout;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    StorageReference rootStorage = FirebaseStorage.getInstance().getReference();
    private Spinner selector,subjectSpinner;

    String Name = MainActivity.Name;
    String ScholarNo = MainActivity.ScholarNo;
    String Year=MainActivity.Year;
    String Branch = MainActivity.Branch;
    String Section = MainActivity.Section;


    /*TODO
    NOTICE UI
    LOGOUT
    DEVELOPERS INFO
    APP LOCK
    LOGIN SIGNUP LOADING
    HARD CODED STRINGS
     */

    /*GRADIENTS
    PURPLE <gradient android:startColor="#6D19FC"  android:endColor="#7D1FA5" android:angle="0"/>
     GREEN  <gradient android:startColor="#32CD32"  android:endColor="#9AFF9A" android:angle="0"/>
     BLUE   <gradient android:startColor="#00B2EE"  android:endColor="#87CEFF" android:angle="0"/>
     RED    <gradient android:startColor="#FE5F75"  android:endColor="#FC9842" android:angle="0"/>
     DARK BLUE   <gradient android:startColor="#004e92"  android:endColor="#6dd5ed" android:angle="0"/>
     DARK BLUE2   <gradient android:startColor="#130CB7"  android:endColor="#6dd5ed" android:angle="0"/>
     PINKISH   <gradient android:startColor="#dd2476"  android:endColor="#ff512f" android:angle="0"/>
     RED2 <gradient android:startColor="#FEAE96"  android:endColor="#E50914" android:angle="0"/>
     RED3   <gradient android:startColor="#FEAE96"  android:endColor="#E40914" android:angle="0"/>
     DARK BLUE    <gradient android:startColor="#009FFD"  android:endColor="#2A2A72" android:angle="0"/>
     DARK BLUE 2   <gradient android:startColor="#00A4E4"  android:endColor="#2A2A72" android:angle="0"/>
     PURPLE2   <gradient android:startColor="#FFAAFF"  android:endColor="#7D1FA5" android:angle="0"/>
     BLUE                <gradient android:startColor="#00c6ff"  android:endColor="#0072ff" android:angle="0"/>
     TEAL            <gradient android:startColor="#AAFFA9"  android:endColor="#11FFBD" android:angle="0"/>
     */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment,container,false);


        //Make Folders
        File CollegeApp = new File(Environment.getExternalStorageDirectory(),"MANIT+");
        if(!CollegeApp.exists())
            CollegeApp.mkdir();
        File Assignments = new File(CollegeApp.getAbsolutePath(),"Assignments");
        if(!Assignments.exists())
            Assignments.mkdir();
        File Solution = new File(Assignments.getAbsolutePath(),"Solutions");
        if(!Solution.exists())
            Solution.mkdir();
        File Question = new File(Assignments.getAbsolutePath(),"Questions");
        if(!Question.exists())
            Question.mkdir();

        recyclerView = view.findViewById(R.id.RecyclerView2);
        selector = view.findViewById(R.id.Selector);
        subjectSpinner = view.findViewById(R.id.SubjectSpinner);
        floatingActionMenu = view.findViewById(R.id.FloatingButton);
        questionsbutton = view.findViewById(R.id.QuestionsButton);
        solutionsbutton = view.findViewById(R.id.SolutionsButton);
        loading = view.findViewById(R.id.Loading);
        swipeRefreshLayout = view.findViewById(R.id.SwipeRefreshLayout);

        MainActivity.toolbar.setTitle("Assignments");

        ArrayList<String> SpinnerList1 = new ArrayList<String>(Arrays.asList("Solutions","Questions"));
        ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_dropdown_item,SpinnerList1);
        selector.setAdapter(SpinnerAdapter);

        //Filling Subject Spinner
        root.child("Colleges").child("MANIT").child(Year).child(Branch).child("Subjects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> SpinnerList2 = new ArrayList<>();
                SpinnerList2.add("All");
                for (DataSnapshot subject : dataSnapshot.getChildren())
                    SpinnerList2.add(subject.getKey().toString());
                ArrayAdapter<String> SpinnerAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_dropdown_item,SpinnerList2);
                subjectSpinner.setAdapter(SpinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Type Selected
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(subjectSpinner.getSelectedItem()!=null) {
                    if (subjectSpinner.getSelectedItem().toString().equals("All"))
                        displayAllAssignments(selector.getSelectedItem().toString());
                    else
                        displaySubjectAssignments(selector.getSelectedItem().toString(), subjectSpinner.getSelectedItem().toString());

                   /* NotificationCompat.Builder builder = new NotificationCompat.Builder(Assignment.this,"MyNotifications");
                    builder.setContentTitle("MANIT+");
                    builder.setContentText("File Opened");
                    builder.setSmallIcon(R.drawable.ic_check);
                    builder.setProgress(100,100,false);
                    builder.setOnlyAlertOnce(true);
                    final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Assignment.this);
                    notificationManager.notify(1,builder.build());*/

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Subject Selected
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(subjectSpinner.getSelectedItem().toString().equals("All"))
                    displayAllAssignments(selector.getSelectedItem().toString());
                else
                    displaySubjectAssignments(selector.getSelectedItem().toString(),subjectSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Upload Questions
        questionsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignmentUploadFragment.type = "Questions";
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AssignmentUploadFragment()).commit();
                floatingActionMenu.close(true);
            }
        });

        //Upload Solutions
        solutionsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignmentUploadFragment.type = "Solutions";
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AssignmentUploadFragment()).commit();
                floatingActionMenu.close(true);
            }
        });

        //Hide Button On Scroll
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0)
                    floatingActionMenu.hideMenu(true);
                else
                    floatingActionMenu.showMenu(true);
            }
        });


        /*String playerid = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();
        Log.i("Hello",playerid);
                JSONObject body = new JSONObject();
                JSONObject headers = new JSONObject();
                try {
                    body.put("contents",new JSONObject().put("en","Title"));
                    body.put("headings",new JSONObject().put("en","Text"));
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(playerid);
                    body.put("include_player_ids",jsonArray);
                    OneSignal.postNotification(body, new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.i("Hello","Success");
                        }

                        @Override
                        public void onFailure(JSONObject response) {
                            Log.i("Hello",response.toString());
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Hello", e.toString());
                }*/

        //Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(selector.getSelectedItem()!=null && subjectSpinner.getSelectedItem()!=null) {
                    if (subjectSpinner.getSelectedItem().toString().equals("All"))
                        displayAllAssignments(selector.getSelectedItem().toString());
                    else
                        displaySubjectAssignments(selector.getSelectedItem().toString(), subjectSpinner.getSelectedItem().toString());
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        return view;

    }

    //Pdf Class
    public class Pdf
    {
        public String subject,name,url,submissiondate,pages,uploader,uploaddate,size;
        boolean completed;
        public Pdf(String subject,String name,String url,String uploader,String submissiondate, String pages,String uploaddate,String size,boolean completed)
        {
            this.subject=subject;
            this.name=name;
            this.url=url;
            this.uploader=uploader;
            this.submissiondate=submissiondate;
            this.pages=pages;
            this.uploaddate = uploaddate;
            this.size=size;
            this.completed=completed;
        }
    }

    //Function to Fill RecyclerView With All Subjects Assignments
    void displayAllAssignments(String type)
    {
        //For Assignment Solutions
        if (type.equals("Solutions")) {
            final DatabaseReference Solutions = root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Assignments").child("Solutions");
            Solutions.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ArrayList<Pdf> pdfArrayList = new ArrayList<>();

                    for (final DataSnapshot subject : dataSnapshot.getChildren()) {
                        File SubjectFolder = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Assignments/Solutions", subject.getKey());
                        if (!SubjectFolder.exists())
                            SubjectFolder.mkdir();

                        for (DataSnapshot pdf : subject.getChildren()) {
                            String name = pdf.child("File Name").getValue().toString();
                            String uploader = pdf.child("Uploader").getValue().toString();
                            String submission = pdf.child("Submission Date").getValue().toString();
                            String url = pdf.child("URL").getValue().toString();
                            String pages = pdf.child("Pages").getValue().toString() + " Pages";
                            String uploaddate = pdf.child("Upload Date").getValue().toString();
                            String size = "(" + pdf.child("Size").getValue().toString()+")";
                            boolean completed = pdf.child("Users").child(ScholarNo).exists();

                            pdfArrayList.add(new Pdf(subject.getKey().toString(), name, url, uploader, submission, pages, uploaddate, size,completed));
                        }
                    }
                    try {
                        pdfArrayList = Sort(pdfArrayList);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.INVISIBLE);
                    RecyclerAdapter_Assignments recyclerAdapter_assignments = new RecyclerAdapter_Assignments(getContext(), pdfArrayList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(recyclerAdapter_assignments);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            final DatabaseReference Questions = root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Assignments").child("Questions");
            Questions.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ArrayList<Pdf> pdfArrayList = new ArrayList<>();

                    for (final DataSnapshot subject : dataSnapshot.getChildren()) {
                        File SubjectFolder = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Assignments/Questions", subject.getKey());
                        if (!SubjectFolder.exists())
                            SubjectFolder.mkdir();

                        for (DataSnapshot pdf : subject.getChildren()) {
                            String name = pdf.child("File Name").getValue().toString();
                            String uploader = pdf.child("Uploader").getValue().toString();
                            String submission = pdf.child("Submission Date").getValue().toString();
                            String url = pdf.child("URL").getValue().toString();
                            String pages = pdf.child("Pages").getValue().toString() + " Pages";
                            String uploaddate = pdf.child("Upload Date").getValue().toString();
                            String size = "(" + pdf.child("Size").getValue().toString()+")";
                            boolean completed = pdf.child("Users").child(ScholarNo).exists();
                            pdfArrayList.add(new Pdf(subject.getKey().toString(), name, url, uploader, submission, pages, uploaddate, size,completed));
                        }
                    }
                    try {
                        pdfArrayList = Sort(pdfArrayList);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.INVISIBLE);
                    RecyclerAdapter_Assignment_Questions recyclerAdapter_assignments = new RecyclerAdapter_Assignment_Questions(getContext(), pdfArrayList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(recyclerAdapter_assignments);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //Function to Fill RecyclerView With Selected Subject Assignments
    void displaySubjectAssignments(String type,final String subject)
    {
        //For Assignment Solutions
        if(type.equals("Solutions")) {
            root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Assignments").child("Solutions").child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ArrayList<Pdf> pdfArrayList = new ArrayList<>();

                    File SubjectFolder = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Assignments/Solutions", subject);
                    if (!SubjectFolder.exists())
                        SubjectFolder.mkdir();

                    for (DataSnapshot pdf : dataSnapshot.getChildren()) {
                        String name = pdf.child("File Name").getValue().toString();
                        String uploader = pdf.child("Uploader").getValue().toString();
                        String submission = pdf.child("Submission Date").getValue().toString();
                        String url = pdf.child("URL").getValue().toString();
                        String pages = pdf.child("Pages").getValue().toString() + " Pages";String uploaddate = pdf.child("Upload Date").getValue().toString();
                        String size = "(" + pdf.child("Size").getValue().toString()+")";
                        boolean completed = pdf.child("Users").child(ScholarNo).exists();
                        pdfArrayList.add(new Pdf(subject, name, url, uploader, submission, pages, uploaddate, size,completed));
                    }

                    try {
                        pdfArrayList = Sort(pdfArrayList);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.INVISIBLE);
                    RecyclerAdapter_Assignments recyclerAdapter_assignments = new RecyclerAdapter_Assignments(getContext(), pdfArrayList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(recyclerAdapter_assignments);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            //For Assignment Questions

            root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Assignments").child("Questions").child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ArrayList<Pdf> pdfArrayList = new ArrayList<>();

                    File SubjectFolder = new File(Environment.getExternalStorageDirectory() + "/MANIT+/Assignments/Questions", subject);
                    if (!SubjectFolder.exists())
                        SubjectFolder.mkdir();

                    for (DataSnapshot pdf : dataSnapshot.getChildren()) {
                        String name = pdf.child("File Name").getValue().toString();
                        String uploader = pdf.child("Uploader").getValue().toString();
                        String submission = pdf.child("Submission Date").getValue().toString();
                        String url = pdf.child("URL").getValue().toString();
                        String pages = pdf.child("Pages").getValue().toString() + " Pages";
                        String uploaddate = pdf.child("Upload Date").getValue().toString();
                        String size = "(" + pdf.child("Size").getValue().toString() + ")";
                        boolean completed = pdf.child("Users").child(ScholarNo).exists();
                        pdfArrayList.add(new Pdf(subject, name, url, uploader, submission, pages, uploaddate, size,completed));
                    }
                    try {
                        pdfArrayList = Sort(pdfArrayList);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    loading.setVisibility(View.INVISIBLE);
                    RecyclerAdapter_Assignment_Questions recyclerAdapter_assignments = new RecyclerAdapter_Assignment_Questions(getContext(), pdfArrayList);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(recyclerAdapter_assignments);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    //Function To Sort The List By Date Of Submission
    ArrayList<Pdf> Sort(ArrayList<Pdf> arrayList) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date today1 = new Date();
        String s = dateFormat.format(today1);

        Date today = dateFormat.parse(s);

        ArrayList<Pdf> active = new ArrayList<>();
        ArrayList<Pdf> old = new ArrayList<>();

        for(int i=0;i<arrayList.size();i++)
        {
            Date date = dateFormat.parse(arrayList.get(i).submissiondate);
            if(date.compareTo(today)>=0)
                active.add(arrayList.get(i));
            else
                old.add(arrayList.get(i));
        }
        active.sort(new Comparator());
        old.sort(new Comparator());

        ArrayList<Pdf> pdfArrayList = new ArrayList<>();
        for(int i=0;i<active.size();i++)
            pdfArrayList.add(active.get(i));
        for(int i=old.size()-1;i>=0;i--)
            pdfArrayList.add(old.get(i));

        return pdfArrayList;
    };

    //Comparator Class
    class Comparator implements java.util.Comparator<Pdf>
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        public int compare(Pdf pdf1, Pdf pdf2)
        {
            try {
                return dateFormat.parse(pdf1.submissiondate).compareTo(dateFormat.parse(pdf2.submissiondate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}
