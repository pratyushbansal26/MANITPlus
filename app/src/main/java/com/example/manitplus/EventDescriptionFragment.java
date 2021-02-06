package com.example.manitplus;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

public class EventDescriptionFragment extends Fragment {

    public static EventClass event;
    private String purl;
    private ImageView imgview;
    private TextView txtTitle,txtDescription,txtSocietyname,text;

    private static final String TAG = "EventDescriptionFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_description, container, false);


        txtSocietyname = view.findViewById(R.id.SocietyName);
        text = view.findViewById(R.id.txtLink);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        txtTitle = view.findViewById(R.id.txtTitle1);
        txtDescription = view.findViewById(R.id.txtDescription1);
        imgview = view.findViewById(R.id.posterImg);

        String description = event.getDescription();
        String url = event.getURL();
        purl = event.getPurl();
        String society = event.getSocietyname();
        Picasso.get().load(purl).into(imgview);

        txtDescription.setText(description);
        text.setText(url);
        txtSocietyname.setText(society);
        txtTitle.setText(event.getTitle());

        return view;
    }

}