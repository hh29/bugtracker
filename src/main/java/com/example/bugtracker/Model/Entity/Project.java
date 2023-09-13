package com.example.bugtracker.Model.Entity;


import java.time.LocalDate;
import java.util.Objects;

public class Project {
    private int projectId;
    private String projectName;
    private String projectDescription;
    private String status;
    private String priority;
    private LocalDate startDate;
    private LocalDate endDate;
    private int projectManagerId;
    private String managerFirstName;
    private String managerLastName;

    // Constructors
    public Project() {
    }

    public Project(int projectId, String projectName, String status) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.status = status;
    }

    public Project(int projectId, String projectName, String projectDescription, String status, String priority,
                   LocalDate startDate, LocalDate endDate, int projectManagerId, String managerFirstName,
                   String managerLastName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectManagerId = projectManagerId;
        this.managerFirstName = managerFirstName;
        this.managerLastName = managerLastName;
    }




    // Getters and Setters
    // (You can use your IDE to generate these automatically)

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getProjectManagerId() {
        return projectManagerId;
    }

    public void setProjectManagerId(int projectManagerId) {
        this.projectManagerId = projectManagerId;
    }

    public String getManagerFirstName() {
        return managerFirstName;
    }

    public void setManagerFirstName(String managerFirstName) {
        this.managerFirstName = managerFirstName;
    }

    public String getManagerLastName() {
        return managerLastName;
    }

    public void setManagerLastName(String managerLastName) {
        this.managerLastName = managerLastName;
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", projectDescription='" + projectDescription + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", startDate=" + startDate +
                ", enddate=" + endDate +
                ", projectManagerId=" + projectManagerId +
                ", managerFirstName='" + managerFirstName + '\'' +
                ", managerLastName='" + managerLastName + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId == project.projectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }


}


