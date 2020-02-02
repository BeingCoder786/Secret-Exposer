package com.mnnit.secretexposer;
public class User {

    public String fullname,email,password,repassword;

    public User(){

    }
    public User(String fullname, String email, String password,String repassword) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.repassword=repassword;
    }
}
