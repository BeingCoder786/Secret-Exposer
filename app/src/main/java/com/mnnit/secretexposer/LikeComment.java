package com.mnnit.secretexposer;

import java.util.ArrayList;
import java.util.HashMap;

public class LikeComment {
    private HashMap<String,Like> likes;
    private ArrayList<Comment> comments;
    public LikeComment(){
        likes=new HashMap<String,Like>();
        comments=new ArrayList<Comment>();
    }
    public void addLike(String uid ,Like like){
        likes.put(uid,like);
    }
    public void addComment(String uid,Comment comment){
        comments.add(comment);
    }
    public void removeLike(String uid){
        likes.remove(uid);
    }
    public void removeComment(int position){
        comments.remove(position);
    }

}
