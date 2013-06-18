html extractor
===============

### Install prepare ###

1. install maven

    [Maven](http://maven.apache.org/)

2. install Summary native lib

    ```
    cd docs/lib
    mvn install:install-file -Dfile="Summary.jar" -DgroupId=com.zctech.zcas -DartifactId=summary-native -Dversion=1.0 -Dpackaging=jar
    ```

### Config source ###

1. config database info

    src/main/resources/configuration.properties:

    ```
    ### jdbc config
    jdbc.driver=com.mysql.jdbc.Driver
    jdbc.url=jdbc:mysql://localhost:3306/zcas?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8
    jdbc.username=zcas
    jdbc.password=zcas
    ```

2. config summary native library

    src/main/resources/constants.properties:

    ```
    ### summary native config
    zcas.conf = 
    zcas.api = native
    ```
