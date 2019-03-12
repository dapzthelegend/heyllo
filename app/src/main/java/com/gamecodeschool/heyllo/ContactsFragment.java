package com.gamecodeschool.heyllo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    private Button btnConnectWithFriends;



    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_contacts, container, false);
        btnConnectWithFriends = view.findViewById(R.id.btnConnectWithFriends);
        btnConnectWithFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findFriendsIntent = new Intent(getContext(), FindFriendsActivity.class);
                startActivity(findFriendsIntent);
            }
        });

        return  view;
    }

}
