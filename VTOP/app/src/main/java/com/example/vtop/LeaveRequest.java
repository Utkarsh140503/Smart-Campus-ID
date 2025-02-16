package com.example.vtop;

public class LeaveRequest {
    private String leaveId;
    private String leaveType;
    private String fromDate;
    private String toDate;
    private String visitingPlace;
    private String proctor;
    private String reason;

    public LeaveRequest() {
        // Default constructor required for Firebase
    }

    public LeaveRequest(String leaveId, String leaveType, String fromDate, String toDate, String visitingPlace, String proctor, String reason) {
        this.leaveId = leaveId;
        this.leaveType = leaveType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.visitingPlace = visitingPlace;
        this.proctor = proctor;
        this.reason = reason;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getVisitingPlace() {
        return visitingPlace;
    }

    public void setVisitingPlace(String visitingPlace) {
        this.visitingPlace = visitingPlace;
    }

    public String getProctor() {
        return proctor;
    }

    public void setProctor(String proctor) {
        this.proctor = proctor;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
