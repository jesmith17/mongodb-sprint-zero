package com.mongodb.ms0.example.javasample.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.ms0.example.javasample.models.Customer;
import com.mongodb.ms0.example.javasample.models.Patient;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Service
public class PatientDAO {

    private MongoCollection<Patient> collection;

    @Autowired
    public PatientDAO(@Qualifier("mongoSecureClient") MongoClient client) {
        MongoDatabase database = client.getDatabase("csfle");
        this.collection = database.getCollection("patients", Patient.class);
    }



    public Patient getPatientById(String id){

        return collection.find(eq("_id", new ObjectId(id))).first();
    }

    public Patient getPatientBySSN(String ssn){

        return collection.find(eq("ssn", ssn)).first();
    }


    public Patient createPatient(Patient patient) {
        ObjectId id = collection.insertOne(patient).getInsertedId().asObjectId().getValue();
        patient.setId(id);
        return patient;

    }

    public List<Patient> patientSearch(String name){
        List<Patient> patients = new ArrayList<>();
        List aggregate = Arrays.asList(
                Aggregates.match(eq("name", "name"))
        );
        this.collection.aggregate(aggregate).forEach(patient -> patients.add((Patient)patient));
        return patients;

    }


}
