package com.ABCD.doctor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Doctor {
    @Id
    private String regNo;
    private String name;
    private String specialization;

    // Getters and Setters
}
