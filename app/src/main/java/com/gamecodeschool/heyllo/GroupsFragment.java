package com.gamecodeschool.heyllo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mArrayList = new ArrayList<>();
    private DatabaseReference mDatabaseReference;



    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView =  inflater.inflate(R.layout.fragment_groups, container, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        //initialize the fileds on the group fragment
       InitializeFields();
        //Retrieve the group names from the firebase database, store it in an arraylist 
        //and diplay it with the listView using arrayAdapter
        RetrieveAndDisplayGroups();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);

            }
        });



    return groupFragmentView;
    }



    private void InitializeFields() {
        mListView = (ListView) groupFragmentView.findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mArrayList);
        mListView.setAdapter(mAdapter);


    }
    private void RetrieveAndDisplayGroups() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();

                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot) iterator.next()).getKey());
                    mArrayList.clear();
                    mArrayList.addAll(set);
                    mAdapter.notifyDataSetChanged();
                   // Log.e("Views", "In Views");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
