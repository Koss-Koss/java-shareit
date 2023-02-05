FROM amazoncorretto:11-alpine
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081
COPY gateway/target/shareit-gateway*.jar shareit-gateway.jar
ENTRYPOINT ["java","-jar","/shareit-gateway.jar", "-cp"]