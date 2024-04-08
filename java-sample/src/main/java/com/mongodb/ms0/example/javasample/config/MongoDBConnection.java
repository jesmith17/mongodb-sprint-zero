package com.mongodb.ms0.example.javasample.config;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateEncryptedCollectionParams;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.ms0.example.javasample.models.Customer;
import com.mongodb.ms0.example.javasample.models.Patient;
import org.bson.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource("classpath:application.properties")
public class MongoDBConnection {


    public Map<String, Object> keyMap = new HashMap<>();
    public Map<String, Map<String, Object>> kmsProviders = new HashMap<>();

    @Value("${encryption.keyCollectionName}")
    private String keyCollectionName;

    public String base64DEK;
    private Map<String, BsonDocument> schemaMap = new HashMap<>();

    @Value("${mongo.uri}")
    private String uri;

    @Value("${encryption.cryptLibSharedPath}")
    private String cryptLibSharedPath;

    @Value("${encryption.localKeyFile}")
    private String localKeyFile;

    @Value("${encryption.databaseName}")
    private String databaseName;

    @Value("${encryption.collectionName}")
    private String collectionName;

    private String keyVaultNamespace = this.databaseName + "." + this.keyCollectionName;





    @Bean(name="mongoClient")
    @Scope(value= ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MongoClient mongoClient() {

        ConnectionString connectionString = new ConnectionString(uri);
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
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
        String path = this.localKeyFile;
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
                .keyVaultNamespace(this.keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();



        /*
        //Use this block for CSFLE without query support

        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
        BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());
        base64DEK = Base64.getEncoder().encodeToString(dataKeyId.getData());


        Document jsonSchema = new Document().append("bsonType", "object").append("encryptMetadata",
                        new Document().append("keyId", new ArrayList<>((Arrays.asList(new Document().append("$binary", new Document()
                                .append("base64", base64DEK)
                                .append("subType", "04")))))))
                .append("properties", new Document()
                        .append("ssn", new Document().append("encrypt", new Document()
                                .append("bsonType", "string")
                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic")))
                        .append("bloodType", new Document().append("encrypt", new Document()
                                .append("bsonType", "string")
                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Random")))
                        .append("medicalRecords", new Document().append("encrypt", new Document()
                                .append("bsonType", "array")
                                .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Random"))));

        schemaMap.put("csfle.patients", BsonDocument.parse(jsonSchema.toJson()));
        */


        /*

        Use this block to do queryable encryption
         */

        BsonDocument queryableEncryptionSchema = new BsonDocument().append("fields",
                new BsonArray(Arrays.asList(
                    new BsonDocument()
                        .append("path", new BsonString("ssn"))
                        .append("bsonType", new BsonString("string"))
                        .append("keyId", new BsonNull())
                        .append("queries", new BsonDocument().append("queryType", new BsonString("equality"))),
                    new BsonDocument()
                        .append("path", new BsonString("bloodType"))
                        .append("keyId", new BsonNull())
                        .append("bsonType",new BsonString( "string")),
                    new BsonDocument()
                        .append("path",new BsonString( "medicalRecords"))
                        .append("keyId", new BsonNull())
                )));



        HashMap<String, BsonDocument> queryableMap = new HashMap<>();
        queryableMap.put(this.databaseName + "." + this.collectionName, queryableEncryptionSchema);

        Map<String, Object> extraOptions = new HashMap<String, Object>();
        extraOptions.put("cryptSharedLibPath", this.cryptLibSharedPath);

        ConnectionString connectionString = new ConnectionString(uri);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .autoEncryptionSettings(AutoEncryptionSettings.builder()
                        .keyVaultNamespace(this.keyVaultNamespace)
                        .kmsProviders(kmsProviders)
                        //.schemaMap(schemaMap)

                        /*
                        Removing the .encryptedFieldsMap from the config seems to be what actually allows it to work.
                        But that goes against the documentation.
                        So we have this field in the autoEncrypt work, but if we use it, it breaks things?
                         */
                        //.encryptedFieldsMap(queryableMap)

                        .extraOptions(extraOptions)
                        .build())
                .build();

        // This is a bit clunky to do this here, but you have to make sure this is done on the collection first so the DB can create the key indexes.
        MongoClient client = MongoClients.create(clientSettings);

        MongoDatabase csfleDB = client.getDatabase(this.databaseName);
        csfleDB.drop();
        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

        CreateEncryptedCollectionParams encryptedCollectionParams = new CreateEncryptedCollectionParams("local");
        encryptedCollectionParams.masterKey(new BsonDocument());
        CreateCollectionOptions options = new CreateCollectionOptions();
        options.encryptedFields(queryableEncryptionSchema);
        clientEncryption.createEncryptedCollection(client.getDatabase(this.databaseName), this.collectionName, options, encryptedCollectionParams);
        return client;

    }



}
