package com.mnnit.secretexposer.group;

import java.util.HashMap;

public class Group {
    private String groupName;
    private String groupOwner;
    private String groupImageUri;
    private String time;
    private HashMap<String,String> members;
    public Group(){}
    public Group(String groupName, String groupOwner, String groupImageUri, String time ,HashMap<String,String> members) {
        this.groupName = groupName;
        this.groupOwner = groupOwner;
        this.groupImageUri = groupImageUri;
        this.time = time;
        this.members = members;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupOwner() {
        return groupOwner;
    }

    public String getGroupImageUri() {
        return groupImageUri;
    }

    public String getTime() {
        return time;
    }

    public HashMap<String,String> getMembers(){
        return members;
    }

}

