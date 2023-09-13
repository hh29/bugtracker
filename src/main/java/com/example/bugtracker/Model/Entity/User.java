package com.example.bugtracker.Model.Entity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.time.LocalDate;
import java.util.Objects;

public class User {

    public String getFullName() {
        return fullName;
    }

    private String fullName;
    private int userId;
    private String username;
    private String password;
    private Roles role;

    private int assignedBugs;
    private LocalDate dob;
    private String email;
    private String firstName;
    private String lastName;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public User(int userId, String username, String firstName, String lastName, String roleName) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roleName = roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    private String roleName;


    public User(int userId, String fullName, String username, String password, Roles role) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public User(int userId, String username, String firstName, String lastName, LocalDate dob, String email, Roles role) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.email = email;
        this.role = role;
    }
    public User(int userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String roleName, int assignedBugs, String firstName, String lastName) {
        this.roleName = roleName;
        this.assignedBugs = assignedBugs;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String firstName, String lastName, String roleName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.roleName = roleName;
    }

    public User(int userId, String firstName, String lastName, String roleName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roleName = roleName;
    }

    public User(int userId, String firstName,String lastName,String roleName,int assignedBugs ) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roleName = roleName;
        this.assignedBugs = assignedBugs;
    }



    public LocalDate getDob() {
        return dob;
    }
    public String getEmail() {
        return email;
    }
    public Roles getRole() {
        return role;
    }
    public User() {
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public int getAssignedBugs() {
        return assignedBugs;
    }
    public String getRoleName() {
        return roleName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, firstName, lastName);
    }
}
