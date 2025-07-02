package tn.test.entities;

import java.time.LocalDate;

public class Worker {


    private int id;
    private String name;
    private String cin;
    private String department;
    private String position;
    private String phone;
    private String email;
    private float salary;
    private String profileImagePath;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private LocalDate creationDate;
    private String status;
    private int totalCongeDays;
    private int usedCongeDays;

    public Worker () {
        // Default constructor
    }

    public Worker(int id, String name, String cin, String department, String position, String phone, String email, float salary, String profileImagePath, String gender, LocalDate dateOfBirth, String address, LocalDate creationDate, String status, int totalCongeDays, int usedCongeDays) {
        this.id = id;
        this.name = name;
        this.cin = cin;
        this.department = department;
        this.position = position;
        this.phone = phone;
        this.email = email;
        this.salary = salary;
        this.profileImagePath = profileImagePath;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.creationDate = creationDate;
        this.status = status;
        this.totalCongeDays = totalCongeDays;
        this.usedCongeDays = usedCongeDays;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalCongeDays() {
        return totalCongeDays;
    }

    public void setTotalCongeDays(int totalCongeDays) {
        this.totalCongeDays = totalCongeDays;
    }

    public int getUsedCongeDays() {
        return usedCongeDays;
    }

    public void setUsedCongeDays(int usedCongeDays) {
        this.usedCongeDays = usedCongeDays;
    }
}
