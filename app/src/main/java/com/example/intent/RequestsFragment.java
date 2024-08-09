package com.example.intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestsFragment extends Fragment {

    private View requestsView;
    private RecyclerView myRequestsList;
    private DatabaseReference chatRequestRef, userRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestsView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        myRequestsList = requestsView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestRef.child(currentUserId), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull final Contacts model) {
                        final String listUserId = getRef(position).getKey();

                        DatabaseReference requestRef = getRef(position).child("request_type").getRef();
                        requestRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String requestType = dataSnapshot.getValue(String.class);
                                    handleRequestType(holder, listUserId, requestType);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.users_request_display_layout, parent, false);
                        return new RequestsViewHolder(view);
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void handleRequestType(final RequestsViewHolder holder, final String listUserId, String requestType) {
        userRef.child(listUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("displayname").getValue(String.class);
                    String userStatus = dataSnapshot.child("status").getValue(String.class);

                    holder.userName.setText(userName);

                    if ("received".equals(requestType)) {
                        holder.userStatus.setText("wants to connect with you");
                        holder.acceptChat.setVisibility(View.VISIBLE);
                        holder.acceptChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                acceptChatRequest(listUserId);
                            }
                        });
                        holder.declineChat.setVisibility(View.VISIBLE);
                        holder.declineChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest(listUserId);
                            }
                        });
                    } else if ("sent".equals(requestType)) {
                        holder.userStatus.setText("You have sent a request to " + userName);
                        holder.acceptChat.setVisibility(View.GONE);
                        holder.declineChat.setVisibility(View.VISIBLE);
                        holder.declineChat.setText("Cancel Chat Request");
                        holder.declineChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest(listUserId);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void acceptChatRequest(final String listUserId) {
        contactsRef.child(currentUserId).child(listUserId).child("Contacts").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(listUserId).child(currentUserId).child("Contacts").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                chatRequestRef.child(currentUserId).child(listUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    chatRequestRef.child(listUserId).child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(getContext(), "New contact added", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest(final String listUserId) {
        chatRequestRef.child(currentUserId).child(listUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(listUserId).child(currentUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Request cancelled", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        Button acceptChat, declineChat;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_request);
            userStatus = itemView.findViewById(R.id.user_status_request);
            acceptChat = itemView.findViewById(R.id.request_accept_button);
            declineChat = itemView.findViewById(R.id.request_decline_button);
        }
    }
}
