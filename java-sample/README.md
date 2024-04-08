# Java Sample App

## Purpose

This app provides a working example of how to use Java and the MongoDB Sync Driver to connect to and work with data in your MongoDB database. 

## Demo Purposes Only

The code in this repository is not intended to be production ready. It is simply designed to provide a working that teams can borrow from to create their own systems. This code base has not been tuned for performance nor has it been checked for any security related issues. ***Do not put this code directly into production***


## Dependencies

The project has the following dependencies

* [Mongodb-driver-sync](https://www.mongodb.com/docs/drivers/java/sync/current/) version 4.9.0
* [Spring-boot](https://spring.io/) - Spring boot is used simply to eliminate the need to also package a app server with the code base. This project makes use of Spring for dependency injection only. It DOES NOT use any of the spring-data approaches.
* Java
* Maven


## Running
To run the project type 

``` mvn spring-boot:run ``` from the command line.

You can also configure this command inside most popular IDE's as needed.


## Accessing

The project will expose a REST api located @ ``` /customer ``` by which you can manipulate the customer data. The following endpoints are already existing

- ``` GET /customer/{id}``` - Pass in the HEX portion of the ObjectId to retrieve a given customer record by ID
- ``` POST /customer ```- Create a new customer record and returns the resultig record
- ``` POSt /customer/search ``` - Uses the aggregation pipeline to do an $or search against first or last name. Takes a single parameter in the request body called ``` name ```
   - This could be a great example of where you can replace the $or with a $search.  Ultimately thats why this exists with an aggregation instead of a traditional fine 


# Queryable Encryption

This has been extended to add in support for Queryable Encryption

This happens by creating a new model ```Patient``` that uses Queryable Encryption to protect a couple fields. 
Internally we do this by having the MongoDBConnection class create 2 different mongoClients beans to be injected

```mongoClient``` is a standard Java connection with the default PojoCodec configured

```mongoSecureClient``` is a mongo client with Queryable Encryption configured. All work to configure encryption is done in the creation for this bean.

To use the queryable encryption portion, you need to do a couple things. Most of these are borrowed from our QuickStart here https://www.mongodb.com/docs/manual/core/queryable-encryption/quick-start/#std-label-qe-quick-start


- [Create a local master key](https://www.mongodb.com/docs/manual/core/queryable-encryption/quick-start/#create-your-encrypted-collection)
- Update the application.properties file with appropriate values
  - Note that the ```Patient``` model annotation will need to be updated to match the value of the collectionName field if changed from the default
- [Download the encryption library](https://www.mongodb.com/docs/manual/core/queryable-encryption/reference/shared-library/)
- Ensure your user has permissions to drop and create databases
  -  For simplicity sake the app drops and re-builds the Db and the keys on each run. I might update it to check for those before hand in the future, but thats for another time

Once the app runs, it will create 2 collections

```patientKeys``` - This is where the encryption keys are stored
```patient``` - this is where patient data is stored (unless you changed the collection names)


There are 2 key API endpoints that also are available. 

### POST /patient

Send a body with payload like 
```angular2html
{
    "name":"Josh Smith",
    "ssn": "987654321",
    "medicalRecords": [
        {
        "weight": 190,
        "bloodPressure":"120/80"
        },
        {
        "weight": 185,
        "bloodPressure":"130/90"
        }
    ]
}
```
This will create a patient records with encryption on the medicalRecords and SSN fields. SSN will be a queryable field and medicalRecords will not


### GET /patient?ssn=xxxxxx

A GET call to this endpoint, using the same SSN provided in the POST will show that you can search for a record by SSN even though it's encrypted in the DB. 










