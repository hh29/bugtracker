package com.example.bugtracker.Model.Entity;

public enum SignupRoles {
    Developer(3),
    ProjectManager(2),
    TechSupport(4),
    Tester(5);

    private final int roleId;

    SignupRoles(int roleId) {
        this.roleId = roleId;
    }

    public int getRoleId() {
        return roleId;
    }

    @Override
    public String toString() {
        String roleName = this.name();
        return switch (roleName) {
            case "ProjectManager" -> "Project Manager";
            case "TechSupport" -> "Tech Support";
            case "Tester" -> "Tester";
            default -> roleName;
        };
    }
}
