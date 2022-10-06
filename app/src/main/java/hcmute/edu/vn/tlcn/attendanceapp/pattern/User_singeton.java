package hcmute.edu.vn.tlcn.attendanceapp.pattern;

import hcmute.edu.vn.tlcn.attendanceapp.model.User;

public class User_singeton {
    private User user = null;
    static User_singeton user_singeTon;
    private User_singeton() {
    }

    public static User_singeton getInstance() {
        if (user_singeTon == null) {
            user_singeTon = new User_singeton();
        }
        return user_singeTon;
    }
    public User getUser(){
        return this.user;
    }
    public void setUser(User user){
        this.user = user;
    }
}
