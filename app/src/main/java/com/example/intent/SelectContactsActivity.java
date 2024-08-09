package com.example.intent;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.intent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectContactsActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private Button createGroupButton,done;

    private DatabaseReference groupsRef;

    private List<String> selectedUserIds; // List to store selected user IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        groupNameEditText = findViewById(R.id.edit_text_group_name);
        createGroupButton = findViewById(R.id.button_create_group);
        done = findViewById(R.id.button_done);

        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        // Retrieve selected user IDs from intent
        selectedUserIds = getIntent().getStringArrayListExtra("selectedUserIds");

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectContactsActivity.this,GroupsFragment.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, "Please enter a group name", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String groupId = groupsRef.push().getKey();

        if (groupId == null) {
            // Handle error when groupId is null
            Toast.makeText(SelectContactsActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set group admin as the current user
        String groupAdminId = currentUserId;

        // Store group details in the database
        Map<String, Object> groupDetails = new HashMap<>();
        groupDetails.put("groupName", groupName);
        groupDetails.put("groupAdmin", groupAdminId);
        groupDetails.put("groupMembers", selectedUserIds); // Store selected user IDs

        groupsRef.child(groupId).setValue(groupDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SelectContactsActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(SelectContactsActivity.this, "Failed to create group", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
