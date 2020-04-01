package com.mnnit.secretexposer.loginSignup;
public class User {

    private String fullname,email,gender,password, key, profileImageUrl;
    public User(){

    }


    public User(String fullname, String email, String password, String gender, String key, String profileImageUrl) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.gender=gender;
        this.key = key;
        this.profileImageUrl = profileImageUrl;
    }
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    public String getFullname(){
        return fullname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail(){
        return email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getGender(){
        return gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getProfileImageUrl(){return profileImageUrl;}
}
