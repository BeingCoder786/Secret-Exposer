package com.mnnit.secretexposer;
public class User {

    public String fullname,email,gender,password,repassword;

    public User(){

    }
    public User(String fullname, String email, String password,String repassword,String gender) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.repassword=repassword;
        this.gender=gender;
    }
}
