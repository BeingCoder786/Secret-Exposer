package com.mnnit.secretexposer.post;

import java.io.Serializable;

public class Post implements Serializable{
    private String id;
    private String postContent;
    private String owner;
    private String groupName;
    private String uri;
    private int postType;
    private boolean anonymous;
    private long counter;
    public Post(){}
    public Post(String id, String postContent, String owener, String groupName, String uri,int postType,boolean anonymous) {
        this.id=id;
        this.postContent = postContent;
        this.owner = owener;
        this.groupName = groupName;
        this.uri = uri;
        this.postType= postType;
        this.anonymous=anonymous;
        this.counter=counter;
    }

    public String getId(){
        return id;
    }
    public String getPostContent() {
        return postContent;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroupName() {

        return groupName;
    }
    public long getCounter(){
        return counter;
    }
    public void setCounter(long counter){
        this.counter=counter;
    }
    public String getUri() {
        return uri;
    }
    public int getPostType(){
        return postType;
    }
    public boolean isAnonymous(){
        return anonymous;
    }
}
