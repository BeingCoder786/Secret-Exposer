package com.mnnit.secretexposer.post;

public class Post {
    private String id;
    private String postContent;
    private String owner;
    private String groupName;
    private String uri;
    private int postType;
    private boolean anonymous;
    public Post(){}
    public Post(String id, String postContent, String owener, String groupName, String uri,int postType,boolean anonymous) {
        this.id=id;
        this.postContent = postContent;
        this.owner = owener;
        this.groupName = groupName;
        this.uri = uri;
        this.postType= postType;
        this.anonymous=anonymous;

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
