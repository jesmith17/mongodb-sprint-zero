package com.mongodb.ms0.example.javasample.service;

import com.mongodb.ms0.example.javasample.dao.PatientDAO;
import com.mongodb.ms0.example.javasample.models.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientDAO dao;


    public Patient getPatientById(String id) {
        return dao.getPatientById(id);
    }


    public Patient createPatient(Patient patient) {
        return dao.createPatient(patient);
    }

    public List<Patient> PatientSearch(String name) {
        return dao.patientSearch(name);
    }
}
