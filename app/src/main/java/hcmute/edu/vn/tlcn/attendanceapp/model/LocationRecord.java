package hcmute.edu.vn.tlcn.attendanceapp.model;

import java.io.Serializable;

public class LocationRecord implements Serializable {
    private Double latitude;
    private Double longitude;

    public LocationRecord(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationRecord() {
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
