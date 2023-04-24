package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Feedback implements Serializable {
    private String userUUID;
    private String detail;
    private ArrayList<String> images;
    private String contact;
    private Date createAt;
    private boolean seen;

    public Feedback(String userUUID, String detail, ArrayList<String> images, String contact) {
        this.userUUID = userUUID;
        this.detail = detail;
        this.images = images;
        this.contact = contact;
        this.seen = false;
        this.createAt = new Date();
    }

    public Feedback(String userUUID, String detail, String contact) {
        this.userUUID = userUUID;
        this.detail = detail;
        this.contact = contact;
        this.seen = false;
        this.createAt = new Date();
    }

    public Feedback() {
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
