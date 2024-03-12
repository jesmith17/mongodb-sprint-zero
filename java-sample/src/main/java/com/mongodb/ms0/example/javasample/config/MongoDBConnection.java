package com.mongodb.ms0.example.javasample.config;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.ms0.example.javasample.models.Customer;
import com.mongodb.ms0.example.javasample.models.Patient;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.util.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
@Service
public class MongoDBConnection {


    public Map<String, Object> keyMap = new HashMap<>();
    public Map<String, Map<String, Object>> kmsProviders = new HashMap<>();

    private String keyVaultNamespace = "csfle.patient";

    public String base64DEK;
    private Map<String, BsonDocument> schemaMap = new HashMap<>();

    private String uri = "mongodb://localhost:27017";



    @Bean(name="mongoClient")
    @Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MongoClient mongoClient() {

        ConnectionString connectionString = new ConnectionString(uri);

        ClassModel<Customer> customerPojo = ClassModel.builder(Customer.class).build();
        //ClassModel<Patient> patientPojo = ClassModel.builder(Patient.class).build();
        PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(customerPojo).build();


        CodecRegistry codecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        // Replace the uri string with your MongoDB deployment's connection string
        return MongoClients.create(clientSettings);
    }



    @Bean(name="mongoSecureClient")
    @Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MongoClient mongoSecureClient(){
        String kmsProvider = "local";
        String path = "/Users/josh.smith/.kmip/customer-master-key.txt";
        byte[] localMasterKeyRead = new byte[96];



        try (FileInputStream fs = new FileInputStream(path)){
            if (fs.read(localMasterKeyRead) < 96){
                throw new RuntimeException("Did not read the expected number of bytes");
            }
            keyMap.put("key", localMasterKeyRead);
            kmsProviders.put("local", keyMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(uri))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();

        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
        BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());
        base64DEK = Base64.getEncoder().encodeToString(dataKeyId.getData());


        Document jsonSchema = new Document().append("bsonType", "object").append("encryptMetadata",
                        new Document().append("keyId", new ArrayList<>((Arrays.asList(new Document().append("$binary", new Document()
                                .append("base64", base64DEK)
                                .append("subType", "04")))))))
                .append("properties", new Document()
                        .append("ssn", new Document().append("encrypt", new Document()
                                .append("bsonType", "int")
                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic")))
                        .append("bloodType", new Document().append("encrypt", new Document()
                                .append("bsonType", "string")
                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Random")))
                        .append("medicalRecords", new Document().append("encrypt", new Document()
                                .append("bsonType", "array")
                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Random")))
                        .append("insurance", new Document()
                                .append("bsonType", "object")
                                .append("properties",
                                        new Document().append("policyNumber", new Document().append("encrypt", new Document()
                                                .append("bsonType", "int")
                                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic"))))));

        schemaMap.put(keyVaultNamespace, BsonDocument.parse(jsonSchema.toJson()));

        Map<String, Object> extraOptions = new HashMap<String, Object>();
        extraOptions.put("cryptSharedLibPath", "/Users/josh.smith/Projects/mongodb-sprint-zero/java-sample/mongo-crypt/lib/mongo_crypt_v1.dylib");

        ConnectionString connectionString = new ConnectionString(uri);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .autoEncryptionSettings(AutoEncryptionSettings.builder()
                        .keyVaultNamespace(keyVaultNamespace)
                        .kmsProviders(kmsProviders)
                        .schemaMap(schemaMap)
                        .extraOptions(extraOptions)
                        .build())
                .build();

        return MongoClients.create(clientSettings);

    }



}
