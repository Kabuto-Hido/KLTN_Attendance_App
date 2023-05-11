package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;
import java.util.Date;

public class UpdateHistory implements Serializable {
    private String id;
    private String performer;
    private String editedPerson;
    private Date implDate;
    private String description;
    private String reason;

    public UpdateHistory(String id, String performer, String editedPerson, Date implDate, String description, String reason) {
        this.id = id;
        this.performer = performer;
        this.editedPerson = editedPerson;
        this.implDate = implDate;
        this.description = description;
        this.reason = reason;
    }

    public UpdateHistory() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getEditedPerson() {
        return editedPerson;
    }

    public void setEditedPerson(String editedPerson) {
        this.editedPerson = editedPerson;
    }

    public Date getImplDate() {
        return implDate;
    }

    public void setImplDate(Date implDate) {
        this.implDate = implDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
