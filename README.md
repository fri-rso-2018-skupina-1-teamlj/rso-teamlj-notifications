# RSO: notifications microservice

## Prerequisites

```bash
docker run -d --name pg-notifications -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=notificationTable -p 5432:5432 postgres:latest
```
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
