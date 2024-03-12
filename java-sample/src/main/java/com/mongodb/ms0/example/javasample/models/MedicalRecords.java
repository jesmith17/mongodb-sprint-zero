package com.mongodb.ms0.example.javasample.models;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class MedicalRecords {

    @BsonProperty
    private Integer weight;
    @BsonProperty
    private String bloodPresssure;

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getBloodPresssure() {
        return bloodPresssure;
    }

    public void setBloodPresssure(String bloodPresssure) {
        this.bloodPresssure = bloodPresssure;
    }
}
