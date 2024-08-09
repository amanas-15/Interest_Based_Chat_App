package com.example.intent;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class Contacts {

    public String displayname, status, id, adminName, groupmember;
    private String groupName;
    private String groupAdmin;
    private List<String> selectedContacts;
    private String userId;
    private boolean selected;
    private boolean adminSelected; // New field to indicate admin selection

    public Contacts() {}

    public Contacts( String status,String displayname,String adminName, String groupmember,String groupName, String groupAdmin, List<String> selectedContacts) {
        this.displayname = displayname;
        this.status = status;
        this.id = id;
        this.selected = false;
        this.adminSelected = false;
        this.adminName = adminName; // Corrected to assign adminName parameter to adminName field
        this.groupmember = groupmember;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.groupName = groupName;
        this.groupAdmin = groupAdmin;
        this.selectedContacts = selectedContacts;
    }

    public Contacts(String groupName, String groupAdmin, List<String> selectedContacts) {
    }


    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getId() {
        return id;
   }

    public boolean isAdminSelected() {
        return adminSelected;
    }

    public void setAdminSelected(boolean adminSelected) {
        this.adminSelected = adminSelected;
    }

    public String getGroupmember() {
        return id;
    }

    public void setGroupmember(String groupmember) {
        this.groupmember = groupmember;
    }
    public String getUserId() {
        return userId;
    }
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public List<String> getSelectedContacts() {
        return selectedContacts;
    }

    public void setSelectedContacts(List<String> selectedContacts) {
        this.selectedContacts = selectedContacts;
    }
    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String username) {
        this.displayname = username;
    }

}
