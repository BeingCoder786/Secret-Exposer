package com.mnnit.secretexposer.post;

public class Comment {
    private String commentContent;
    private String uid;
    private String timeStamp;
    public Comment(String commentContent,String uid,String timeStamp){
        this.commentContent=commentContent;
        this.uid=uid;
        this.timeStamp=timeStamp;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public String getUid() {
        return uid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
