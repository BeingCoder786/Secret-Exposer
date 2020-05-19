package com.mnnit.secretexposer.ui.notification;

public class Notification{
    private String notificationText;
    private String time;
    private String profileImageUrl;
    public Notification(){}
    public Notification(String notificationText,String time,String profileImageUrl){
        this.notificationText=notificationText;
        this.time=time;
        this.profileImageUrl=profileImageUrl;
    }

    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public void setProfileImageUrl( String profileImageUrl ){
        this.profileImageUrl = profileImageUrl;
    }

    public String getNotificationText(){
        return notificationText;
    }

    public void setNotificationText( String notificationText ){
        this.notificationText = notificationText;
    }

    public String getTime(){
        return time;
    }

    public void setTime( String time ){
        this.time = time;
    }
}
