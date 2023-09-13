package com.example.bugtracker.Model.Entity;

    public class Reporter {
        private int reporterId;
        private String firstName;
        private String lastName;

        public int getReporterId() {
            return reporterId;
        }

        public void setReporterId(int reporterId) {
            this.reporterId = reporterId;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

