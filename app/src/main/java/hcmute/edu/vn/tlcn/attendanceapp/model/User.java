package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class User implements Serializable {
    public String uuid;
    public String fullName;
    public String phone;
    private String password;
    public String birthday;
    public String description;
    public Boolean sex;
    public String avatar;
    public Integer role;
    public String qrcode;

    public User(String uuid, String fullName, String phone, String password, String birthday,
                String description, Boolean sex, String avatar, Integer role, String qrcode) {
        this.uuid = uuid;
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.birthday = birthday;
        this.description = description;
        this.sex = sex;
        this.avatar = avatar;
        this.role = role;
        this.qrcode = qrcode;
    }

    public User() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

}
