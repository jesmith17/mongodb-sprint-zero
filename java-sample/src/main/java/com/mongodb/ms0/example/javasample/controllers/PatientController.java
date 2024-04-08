package com.mongodb.ms0.example.javasample.controllers;

import com.mongodb.ms0.example.javasample.models.Patient;
import com.mongodb.ms0.example.javasample.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="patient", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class PatientController {

    @Autowired
    public PatientService service;


    @GetMapping(value="{id}")
    public Patient getPatientById(@PathVariable("id") String id) {
        return service.getPatientById(id);
    }

    @GetMapping()
    public Patient getPatientBySSN(@RequestParam String ssn) {
        return service.getPatientBySSN(ssn);
    }

    @PostMapping
    public Patient createPatient(@RequestBody Patient Patient) {
        return service.createPatient(Patient);
    }


    @PostMapping(value="search")
    public List<Patient> PatientSearch(@RequestBody Map<String, String> values) {
        return service.PatientSearch(values.get("name"));
    }


}
