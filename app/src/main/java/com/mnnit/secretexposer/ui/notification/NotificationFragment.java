package com.mnnit.secretexposer.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;

public class NotificationFragment extends Fragment {

    private NotificationViewModel slideshowViewModel;
    private String uid;
    private LinearLayout linearLayout;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(NotificationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notification, container, false);
        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        linearLayout = root.findViewById(R.id.notification_layout);
        new Thread(new Runnable(){
            @Override
            public void run(){
                showNotification();
            }
        }).start();
        return root;
    }
    private void showNotification(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid)
                .child("Notification")
                .limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                        LinkedList <View> list=new LinkedList<View>();
                        for(DataSnapshot notificationSnapshot:dataSnapshot.getChildren()) {
                            Notification notification=notificationSnapshot.getValue(Notification.class);
                            View view = getLayoutInflater()
                                    .inflate(R.layout.notification_container, null);
                            ImageView profileImage = view.findViewById(R.id.profile_image);
                            TextView text = view.findViewById(R.id.notification);
                            if(!notification.getProfileImageUrl().isEmpty())
                            Picasso.with(getContext())
                                    .load(notification.getProfileImageUrl())
                                    .into(profileImage);
                            text.setText(notification.getNotificationText());
                            view.setPadding(0,10,10,5);
                            list.addFirst(view);
                        }
                        while(list.size()>0)
                        linearLayout.addView(list.pollFirst());

                    }
                    @Override
                    public void onCancelled( @NonNull DatabaseError databaseError ){

                    }
                });
    }

}