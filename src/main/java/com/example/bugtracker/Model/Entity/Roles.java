package com.example.bugtracker.Model.Entity;

public enum Roles {
    Admin(1,"Admin"),
    ProjectManager(2,"Project Manager"),
    Developer(3,"Developer"),
    TechSupport(4,"Tech Support"),
    Tester(5,"Tester");


    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    private int roleId;
    private String displayText;

    private Roles(int roleId, String displayText) {
        this.roleId = roleId;
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
    public static Roles getRoleEnum(String roleTitle) {
        for (Roles role : Roles.values()) {
            if (role.getDisplayText().equalsIgnoreCase(roleTitle)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant for role title: " + roleTitle);
    }
    public int getRoleId() {
        return roleId;
    }

    @Override
    public String toString() {
        return displayText;
    }


}
