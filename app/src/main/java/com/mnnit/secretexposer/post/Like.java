package com.mnnit.secretexposer.post;

public class Like {
    private String uid;
    private String postId;
    private String timeStamp;
    public Like(String uid,String postId,String timeStamp){
        this.uid=uid;
        this.postId=postId;
        this.timeStamp=timeStamp;
    }
    public String getUid() {
        return uid;
    }

    public String getPostId() {
        return postId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
