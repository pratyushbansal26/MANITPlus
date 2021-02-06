package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RateUsFragment extends Fragment {

    private EditText suggestionsBox;
    private Button star1,star2,star3,star4,star5 , submit;
    private TextView averageRating, ratingcount;
    int flag = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rate_us,container,false);

        suggestionsBox = (EditText) view.findViewById(R.id.suggestionsBox);
        star1 = (Button) view.findViewById(R.id.star1);
        star2 = (Button) view.findViewById(R.id.star2);
        star3 = (Button) view.findViewById(R.id.star3);
        star4 = (Button) view.findViewById(R.id.star4);
        star5 = (Button) view.findViewById(R.id.star5);
        averageRating = (TextView) view.findViewById(R.id.averageRating);
        ratingcount = view.findViewById(R.id.RatingCount);
        submit = (Button) view.findViewById(R.id.submit);

        MainActivity.toolbar.setTitle("Rate Us");

        final DatabaseReference suggestion = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Suggestions")
                .child(MainActivity.ScholarNo);
        Date today = new Date();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        final String time = df.format(today);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(suggestionsBox.getText().toString().equals("")) {
                    Toast.makeText(getActivity() , "Please Enter Your Suggestion..." , Toast.LENGTH_SHORT).show();
                }
                else {
                    suggestion.child(time).setValue(suggestionsBox.getText().toString());
                    suggestionsBox.setText("");
                    Toast.makeText(getActivity(),"Thank You For Your Suggestion!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        final DatabaseReference rating = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT")
                .child("Ratings").child(MainActivity.ScholarNo);

        rating.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {

                    String  r = snapshot.getValue().toString();

                    if(r.equals("1")) {
                        star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star2.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                        star3.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                        star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                        star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    }
                    else if(r.equals("2")) {
                        star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star3.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                        star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                        star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    }
                    else if(r.equals("3")) {
                        star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star3.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                        star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    }
                    else if(r.equals("4")) {
                        star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star3.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star4.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    }
                    else {
                        star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star3.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star4.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                        star5.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                    }
                }
                else {
                    star1.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    star2.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    star3.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                    star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                }

                if(flag==1)
                    Toast.makeText(getContext(),"Thanks For Your Rating!",Toast.LENGTH_SHORT).show();
                else
                    flag=1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star2.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                star3.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                rating.setValue("1");

            }
        });

        star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star3.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                rating.setValue("2");

            }
        });

        star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star3.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star4.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                rating.setValue("3");

            }
        });

        star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star3.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star4.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star5.setBackgroundResource(R.drawable.ic_baseline_star_uncoloured);
                rating.setValue("4");

            }
        });

        star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                star1.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star2.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star3.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star4.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                star5.setBackgroundResource(R.drawable.ic_baseline_star_coloured);
                rating.setValue("5");

            }
        });

        final DatabaseReference average = FirebaseDatabase.getInstance().getReference().child("Colleges").child("MANIT").child("Ratings");
        average.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long total = snapshot.getChildrenCount();
                long sum = 0;
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    sum += Long.parseLong(dataSnapshot.getValue().toString());
                }

                if(total != 0) {
                    double avg = ((double) sum) / total;
                    averageRating.setText(String.format("%.1f",avg));
                }
                else averageRating.setText("0");

                ratingcount.setText("("+total+" Ratings)");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}