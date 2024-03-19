package com.mongodb.ms0.example.javasample.models;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class MedicalRecords {

    @BsonProperty
    private Integer weight;
    @BsonProperty
    private String bloodPressure;

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }
}
