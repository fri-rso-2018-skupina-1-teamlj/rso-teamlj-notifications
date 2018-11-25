# RSO: notifications microservice

## Prerequisites

No database for this one, it just gets all the info.

Local run (warning: debugger needs to be attached):
```
java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=y -jar api/target/notifications-api-1.0.0-SNAPSHOT.jar
```


App start:
```
docker build -t rso-teamlj-notifications:1.0 .
docker run -p 8084:8084 rso-teamlj-notifications:1.0
to change network host: docker run -p 8084:8084 --net=host rso-teamlj-notifications:1.0
```
