package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;
import java.util.Date;

public class UpdateHistory implements Serializable {
    private String performer;
    private Date implDate;
    private String description;
    private String reason;

    public UpdateHistory(String performer, Date implDate, String description, String reason) {
        this.performer = performer;
        this.implDate = implDate;
        this.description = description;
        this.reason = reason;
    }

    public UpdateHistory() {
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
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
