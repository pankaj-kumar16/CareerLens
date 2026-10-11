package com.careerlens.backend.dto;

public class ApplicationNotesUpdateRequest {
    private String recruiterNotes;

    public ApplicationNotesUpdateRequest() {}

    public ApplicationNotesUpdateRequest(String recruiterNotes) {
        this.recruiterNotes = recruiterNotes;
    }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }
}
