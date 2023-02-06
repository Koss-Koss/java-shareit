FROM amazoncorretto:11-alpine
COPY target/*.jar shareit-server.jar
ENTRYPOINT ["java","-jar","/shareit-server.jar"]

#FROM amazoncorretto:11-alpine
#ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9091
#COPY server/target/shareit-server*.jar shareit-server.jar
#ENTRYPOINT ["java","-jar","/shareit-server.jar", "-cp"]