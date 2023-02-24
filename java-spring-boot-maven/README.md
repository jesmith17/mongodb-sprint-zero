# MongoDB Sprint Zero (MS0) Code Assets for Spring Boot (Maven)

A template modeled after [ms0.py](../ms0.py) for Java Spring Boot. The design of this application is loosely based on the [Spring Boot Integration with MongoDB Tutorial](https://www.mongodb.com/compatibility/spring-boot).

## Getting Started
### Set Your Connection String
Set your connection string in the [application.properties](./ms0/src/main/resources/application.properties) file.

### Package The Application
```zsh
ms0✗ mvn package
```
## Running The Application
```zsh
ms0✗ java -jar target/ms0-0.0.1-SNAPSHOT.jar <arg>
```

The following arguments are supported:
* insert 
* findIt 
* updateMatched 
* replaceMatched
* upsert 
* deleteAll
* currentOp

For example:
```zsh
✗ java -jar target/ms0-0.0.1-SNAPSHOT.jar insert

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.0.2)

Inserting a Thing...

✗ 
```

# Reviewing the Source
Check out [Ms0Application.java](https://github.com/wbleonard/mongodb-sprint-zero/blob/SpringBoot/java-spring-boot-maven/ms0/src/main/java/com/example/ms0/Ms0Application.java)

All criticism is welcome :-).

