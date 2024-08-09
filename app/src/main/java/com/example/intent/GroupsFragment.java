package com.example.intent;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.intent.GroupChatActivity;
import com.example.intent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private ListView listViewGroups;
    private ArrayAdapter<String> arrayAdapter;
    private DatabaseReference groupRef;
    private Button deleteButton;
    private boolean isLongPress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        initializeViews(groupFragmentView);
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        retrieveAndDisplayGroups();

        listViewGroups.setOnItemLongClickListener((parent, view, position, id) -> {
            isLongPress = true;
            toggleCheckboxVisibility(position);
            return true;
        });

        listViewGroups.setOnItemClickListener((parent, view, position, id) -> {
            if (!isLongPress) {
                String selectedGroup = arrayAdapter.getItem(position);
                navigateToGroupChat(selectedGroup);
            } else {
                toggleCheckboxVisibility(position);
            }
        });

        deleteButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                SparseBooleanArray checkedItems = listViewGroups.getCheckedItemPositions();
                if (checkedItems != null) {
                    for (int i = 0; i < arrayAdapter.getCount(); i++) {
                        if (checkedItems.get(i)) {
                            String groupName = arrayAdapter.getItem(i);
                            removeGroupMember(groupName, currentUser.getUid());
                        }
                    }
                } else {
                    Log.e(TAG, "Checked items array is null.");
                }
            } else {
                Log.e(TAG, "User not logged in.");
            }
        });

        return groupFragmentView;
    }

    private void initializeViews(View view) {
        listViewGroups = view.findViewById(R.id.list_view_groups);
        arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        listViewGroups.setAdapter(arrayAdapter);
        deleteButton = view.findViewById(R.id.buttonDeleteGroup);
        ArrayList<String> selectedGroups = new ArrayList<>();
    }

    private void retrieveAndDisplayGroups() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> groupNames = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupAdmin = snapshot.child("groupAdmin").getValue(String.class);
                    boolean isMember = false;

                    // Check if the current user is the group admin
                    if (groupAdmin != null && groupAdmin.equals(currentUserId)) {
                        isMember = true; // Admin is always a member
                    } else {
                        // Check if the current user is a member of the group
                        DataSnapshot groupMembersSnapshot = snapshot.child("groupMembers");
                        for (DataSnapshot memberSnapshot : groupMembersSnapshot.getChildren()) {
                            String memberId = memberSnapshot.getValue(String.class);
                            if (memberId != null && memberId.equals(currentUserId)) {
                                isMember = true;
                                break;
                            }
                        }
                    }

                    if (isMember) {
                        groupNames.add(snapshot.child("groupName").getValue(String.class));
                    }
                }
                arrayAdapter.clear();
                arrayAdapter.addAll(groupNames);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve groups: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deleteButton.setVisibility(View.GONE); // Initially hide the delete button

        listViewGroups.setOnItemLongClickListener((parent, view1, position, id) -> {
            isLongPress = true;
            listViewGroups.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // Change choice mode to MULTIPLE
            toggleCheckboxVisibility(position);
            deleteButton.setVisibility(View.VISIBLE); // Show the delete button
            return true;
        });

        listViewGroups.setOnItemClickListener((parent, view1, position, id) -> {
            if (!isLongPress) {
                String selectedGroup = arrayAdapter.getItem(position);
                navigateToGroupChat(selectedGroup);
            } else {
                toggleCheckboxVisibility(position);
            }
        });

        deleteButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "User not logged in.");
                return;
            }

            SparseBooleanArray checkedItems = listViewGroups.getCheckedItemPositions();
            if (checkedItems == null) {
                Log.e(TAG, "Checked items array is null.");
                return;
            }

            for (int i = 0; i < arrayAdapter.getCount(); i++) {
                if (checkedItems.get(i)) {
                    String groupName = arrayAdapter.getItem(i);
                    String currentUserId = currentUser.getUid();
                    removeGroupMember(groupName, currentUserId);
                }
            }
        });
    }

    private void toggleCheckboxVisibility(int position) {
        boolean isChecked = !listViewGroups.isItemChecked(position);
        listViewGroups.setItemChecked(position, isChecked);
    }

    @Override
    public void onResume() {
        super.onResume();
        isLongPress = false; // Reset long press flag when the fragment resumes
        listViewGroups.clearChoices(); // Clear selected choices
        listViewGroups.setChoiceMode(ListView.CHOICE_MODE_NONE); // Reset choice mode to NONE
        deleteButton.setVisibility(View.GONE); // Hide delete button
    }

    private void removeGroupMember(String groupId, String memberIdToRemove) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);

        groupRef.child("groupMembers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> groupMembers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String memberId = snapshot.getValue(String.class);
                    if (!memberId.equals(memberIdToRemove)) {
                        groupMembers.add(memberId);
                    }
                }
                groupRef.child("groupMembers").setValue(groupMembers)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Member removed successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to remove member", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to remove member", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void navigateToGroupChat(String groupName) {
        Intent groupChatIntent = new Intent(requireContext(), GroupChatActivity.class);
        groupChatIntent.putExtra("groupName", groupName);
        startActivity(groupChatIntent);
    }
}
