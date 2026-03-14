# Learning Spring Boot 3.0 - Chapter 2

    ./mvnw -N wrapper:wrapper -Dmaven=3.9.12
    ./mvnw spring-boot:run

    curl -v localhost:8080/api/videos -d '{"name": "Learning Spring Boot 3"}' -H 'Content-type:application/json'
    curl localhost:8080/api/videos

    ./mvnw generate-resources
    node/npm install --save-dev parcel
