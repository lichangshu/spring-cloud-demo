FROM openjdk:8
VOLUME /tmp

ADD target/ico-auth-0.0.1-SNAPSHOT.jar /opt/app.jar

WORKDIR /opt/

ENV SPRING_PROFILES_ACTIVE staging
ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /opt/app.jar" ]

