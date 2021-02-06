package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {

    static public String OneSignalAppId = "1e32a57d-e651-4b59-942f-ec9667353ed6";

    static String Year;
    static String Branch;
    static String Section;
    static String ScholarNo;
    static  String Name ;
    static  String Hostel ;
    static String PlayerId = null;
    static boolean Admin = false;


    static Toolbar toolbar;
    DrawerLayout drawerLayout;
    public static NavigationView navigationView;
    int backpressed;
    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        However, if the system destroys the activity due to system constraints (such as a configuration change or memory pressure),
         then although the actual Activity instance is gone, the system remembers that it existed. If the user attempts to navigate back
          to the activity, the system creates a new instance of that activity using a set of saved data that describes the state of the activity
           when it was destroyed.
         */

        //Checking If Activity was Destroyed And Restoring Bundle Instance
        /*if(savedInstanceState!=null)
        {
            ScholarNo = savedInstanceState.getString("SCHOLARNO");
            Name = savedInstanceState.getString("NAME");
            Year = savedInstanceState.getString("YEAR");
            Branch = savedInstanceState.getString("BRANCH");
            Section = savedInstanceState.getString("SECTION");
            Hostel = savedInstanceState.getString("HOSTEL");
            PlayerId = savedInstanceState.getString("PLAYERID");
            Admin = savedInstanceState.getBoolean("ADMIN");
        }*/


        SharedPreferences sharedPreferences = getSharedPreferences("User Data", MODE_PRIVATE);


        ScholarNo = sharedPreferences.getString("Scholar No", "");
        Name = sharedPreferences.getString("Name", "");
        Year = sharedPreferences.getString("Year", "");
        Branch = sharedPreferences.getString("Branch", "");
        Section = sharedPreferences.getString("Section", "");
        Hostel = sharedPreferences.getString("Hostel", "");
        PlayerId = sharedPreferences.getString("Player Id", "");


        //Portrait Fixed
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //OneSignal Initialization....
        OneSignal.initWithContext(this);
        OneSignal.setAppId(OneSignalAppId);

        backpressed = 0;

        toolbar = findViewById(R.id.Toolbar);
        drawerLayout = findViewById(R.id.DrawerLayout);
        navigationView = findViewById(R.id.NavigationView);

        //Check For Admin And Banned...
        root.child("Colleges").child("MANIT").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Admins").child(ScholarNo).exists())
                    Admin = true;
                if(snapshot.child("Lock").exists())
                {
                    BannedActivity.message = "SERVICE TEMPORARILY UNAVAILABLE...";
                    Intent intent = new Intent(getApplicationContext(),BannedActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(snapshot.child("Banned").child(ScholarNo).exists())
                {
                    Intent intent = new Intent(getApplicationContext(),BannedActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setCheckedItem(R.id.HomeMenu);
        getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new HomeFragment()).commit();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getTitle().equals("Home"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new HomeFragment()).commit();

                else if (menuItem.getTitle().equals("My Attendance"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AttendanceFragment()).commit();

                else if (menuItem.getTitle().equals("Mess"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new MessFragment()).commit();

                else if (menuItem.getTitle().equals("Assignments"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AssignmentFragment()).commit();

                else if (menuItem.getTitle().equals("Events"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new EventFragment()).commit();

                else if (menuItem.getTitle().equals("Announcements"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AnnouncementsFragment()).commit();

                else if (menuItem.getTitle().equals("Notices"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new NoticesFragment()).commit();

                else if (menuItem.getTitle().equals("Notes"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new NotesFragment()).commit();

                else if (menuItem.getTitle().equals("My Account"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new MyAccountFragment()).commit();

                else if (menuItem.getTitle().equals("How To Use"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new HowToUseFragment()).commit();

                else if (menuItem.getTitle().equals("Rate Us"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new RateUsFragment()).commit();

                else if (menuItem.getTitle().equals("Developers Info"))
                    getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new DevelopersInfoFragment()).commit();

                //Log Out
                if(menuItem.getTitle().equals("Log Out"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Log Out");
                    builder.setMessage("Are You Sure You Want To Log Out?");
                    builder.setNegativeButton("CANCEL",null);
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            auth.signOut();
                            //Clear Player Id
                            root.child("Colleges").child("MANIT").child(Year).child(Branch).child(Section).child("Player Ids").child(ScholarNo).removeValue();

                            Toast.makeText(getApplicationContext(),"Logged Out Successfully...",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });


        //Read And Write Permissions Check
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }


        //Notification Channel to Categorize Notifications Above Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("MyNotifications", "MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }


    //Ask For Read Write Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_DENIED)
            Toast.makeText(getApplicationContext(),"Please Give The Permissions To Use The App...",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            backpressed = 0;
        }
        else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof HomeFragment)
        {
            if(backpressed==1)
                super.onBackPressed();
            else
            {
                backpressed++;
                Toast.makeText(getApplicationContext(),"Press Back Again To Exit",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            backpressed=0;
            if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof AssignmentUploadFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AssignmentFragment()).commit();

            else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof NotesUploadFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new NotesFragment()).commit();

            else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof NoticeUploadFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new NoticesFragment()).commit();

            else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof EventUploadFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new EventFragment()).commit();

            else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof AnnouncementUploadFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AnnouncementsFragment()).commit();

            else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof AttendanceCalenderFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new AttendanceFragment()).commit();

            else if(getSupportFragmentManager().findFragmentById(R.id.Frame_Container) instanceof EventDescriptionFragment)
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new EventFragment()).commit();

            else {
                getSupportFragmentManager().beginTransaction().replace(R.id.Frame_Container, new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.HomeMenu);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("SCHOLARNO", ScholarNo);
        outState.putString("NAME", Name);
        outState.putString("YEAR", Year);
        outState.putString("BRANCH", Branch);
        outState.putString("SECTION", Section);
        outState.putString("HOSTEL", Hostel);
        outState.putString("PLAYERID", PlayerId);
        outState.putBoolean("ADMIN", Admin);

        super.onSaveInstanceState(outState);
    }
}
