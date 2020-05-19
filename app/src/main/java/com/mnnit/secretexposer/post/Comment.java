package com.mnnit.secretexposer.post;

public class Comment {
    private String commentId;
    private String commentContent;
    private String uid;
    private String timeStamp;
    public Comment(){}
    public Comment(String commentId , String commentContent,String uid,String timeStamp){
        this.commentId = commentId;
        this.commentContent=commentContent;
        this.uid=uid;
        this.timeStamp=timeStamp;
    }

    public String getCommentId(){
        return commentId;
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
