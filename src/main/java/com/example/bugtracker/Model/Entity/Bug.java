package com.example.bugtracker.Model.Entity;

import java.time.LocalDate;

import javafx.beans.property.*;

public class Bug {
    private final IntegerProperty bugId = new SimpleIntegerProperty();
    private final StringProperty bugTitle = new SimpleStringProperty();




    public void setReporterId(int reporterId) {
        this.reporterId.set(reporterId);
    }

    public void setProjectId(int projectId) {
        this.projectId.set(projectId);
    }

    private final IntegerProperty reporterId = new SimpleIntegerProperty();
    private final IntegerProperty projectId = new SimpleIntegerProperty();

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public String getProjectName() {
        return projectName.get();
    }

    private final StringProperty projectName = new SimpleStringProperty();





    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateCreated = new SimpleObjectProperty<>();
    private final StringProperty priority = new SimpleStringProperty();

    public StringProperty severityProperty() {
        return severity;
    }

    private final StringProperty severity = new SimpleStringProperty();
    private final StringProperty assigneeFirstName = new SimpleStringProperty();

    public String getAssigneeFirstName() {
        return assigneeFirstName.get();
    }

    public StringProperty assigneeFirstNameProperty() {
        return assigneeFirstName;
    }

    public void setAssigneeFirstName(String assigneeFirstName) {
        this.assigneeFirstName.set(assigneeFirstName);
    }

    public String getAssigneeLastName() {
        return assigneeLastName.get();
    }

    public StringProperty assigneeLastNameProperty() {
        return assigneeLastName;
    }

    public void setAssigneeLastName(String assigneeLastName) {
        this.assigneeLastName.set(assigneeLastName);
    }

    private final StringProperty assigneeLastName = new SimpleStringProperty();

    private final ObjectProperty<Reporter> reporter = new SimpleObjectProperty<>();

    public StringProperty estimatedTimeToCompleteProperty() {
        return estimatedTimeToComplete;
    }

    private final StringProperty estimatedTimeToComplete = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> createdDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> updatedDate = new SimpleObjectProperty<>();

    public ObjectProperty<String> bugDescriptionProperty() {
        return bugDescription;
    }

    private final ObjectProperty<String> bugDescription = new SimpleObjectProperty<>();

    // Constructor
    public Bug() {
    }

    // Getters and setters for properties
    public IntegerProperty bugIdProperty() {
        return bugId;
    }
    public int getBugId() {
        return bugId.get();
    }

    public void setBugId(int bugId) {
        this.bugId.set(bugId);
    }

    public String getBugTitle() {
        return bugTitle.get();
    }

    public StringProperty bugTitleProperty() {
        return bugTitle;
    }

    public void setBugTitle(String bugTitle) {
        this.bugTitle.set(bugTitle);
    }


    public StringProperty projectNameProperty() {
        return projectName;
    }


    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getPriority() {
        return priority.get();
    }

    public StringProperty priorityProperty() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public Reporter getReporter() {
        return reporter.get();
    }

    public ObjectProperty<Reporter> reporterProperty() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter.set(reporter);
    }

    public String getEstimatedTimeToComplete() {
        return estimatedTimeToComplete.get();
    }
    public void setEstimatedTimeToComplete(String estimatedTime) {
        this.estimatedTimeToComplete.set(estimatedTime);
    }

    public LocalDate getCreatedDate() {
        return createdDate.get();
    }

    public ObjectProperty<LocalDate> createdDateProperty() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate.set(createdDate);
    }

    public LocalDate getUpdatedDate() {
        return updatedDate.get();
    }

    public ObjectProperty<LocalDate> updatedDateProperty() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate.set(updatedDate);
    }

    public String getBugDescription() {
        return bugDescription.get();
    }

    public void setBugDescription(String bugDescription) {
        this.bugDescription.set(bugDescription);
    }
    public String getSeverity() {
        return severity.get();
    }
    public void setSeverity(String severity) {
        this.severity.set(severity);
    }




    public int getProjectId() {
        return projectId.get();
    }

    public IntegerProperty projectIdProperty() {
        return projectId;
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public int getReporterId() {
        return reporterId.get();
    }

    public IntegerProperty reporterIdProperty() {
        return reporterId;
    }


}



