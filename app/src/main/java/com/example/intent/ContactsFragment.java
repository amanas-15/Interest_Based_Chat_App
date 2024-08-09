package com.example.intent;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView myContactsList;
    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private boolean isSelectingContacts = false;
    private List<String> selectedContacts = new ArrayList<>();
    public  String group;

    private DatabaseReference groupsRef;
    boolean isSelected;

    private FloatingActionButton fabCreateGroup; // Corrected variable type

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactsList = contactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        fabCreateGroup = contactsView.findViewById(R.id.fab_create_group); // Initialize fabCreateGroup

        return contactsView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int position, @NonNull Contacts contacts) {
                String userIDs = getRef(position).getKey();
                userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                            if(dataSnapshot.child("userState").hasChild("state")){
                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                if(state.equals("online")) {
                                    contactsViewHolder.onlineIcon.setVisibility(View.VISIBLE);
                                } else if(state.equals("offline")){
                                    contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            } else{
                                contactsViewHolder.onlineIcon.setVisibility(View.INVISIBLE);

                            }

                            String userName = dataSnapshot.child("displayname").getValue().toString();
                            String userStatus = dataSnapshot.child("status").getValue().toString();


                            contactsViewHolder.userName.setText(userName);
                            contactsViewHolder.userStatus.setText(userStatus);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                contactsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isSelectingContacts) {
                            // If not selecting contacts, proceed to the profile
                            String visit_user_id = getRef(position).getKey();
                            Intent profileIntent = new Intent(contactsView.getContext(), ProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                        } else {
                            // Toggle selection for the clicked contact
                            contactsViewHolder.checkBox.setChecked(!contactsViewHolder.checkBox.isChecked());
                            // Update selectedContacts list
                            if (contactsViewHolder.checkBox.isChecked()) {
                                selectedContacts.add(userIDs);
                            } else {
                                selectedContacts.remove(userIDs);
                            }
                        }
                    }
                });
                contactsViewHolder.checkBox.setVisibility(isSelectingContacts ? View.VISIBLE : View.GONE);



            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

        myContactsList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    // Toggle selecting contacts mode
                    isSelectingContacts = true;
                    // Show checkboxes for all items
                    for (int i = 0; i < myContactsList.getChildCount(); i++) {
                        View child = myContactsList.getChildAt(i);
                        CheckBox checkBox = child.findViewById(R.id.checkbox_contact);
                        checkBox.setVisibility(View.VISIBLE);
                    }
                }
            });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        // Floating action button click listener to create a group
        fabCreateGroup.setOnClickListener(v -> redirectToSelectContactsActivity(selectedContacts));
    }

    // Method to create a group with selected contacts
    private void redirectToSelectContactsActivity(List<String> selectedUserIds) {
        // Check if any checkboxes are ticked
        if (selectedUserIds.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one contact", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getContext(), SelectContactsActivity.class);
            intent.putStringArrayListExtra("selectedUserIds", (ArrayList<String>) selectedUserIds);
            startActivity(intent);
        }
    }




    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        ImageView onlineIcon;

        CheckBox checkBox;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userStatus = itemView.findViewById(R.id.user_status);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
            checkBox = itemView.findViewById(R.id.checkbox_contact);
        }
    }
}
