FROM openjdk:8
VOLUME /tmp

ADD target/demo-cloud-oauth-server-0.0.1-SNAPSHOT.jar /opt/app.jar

WORKDIR /opt/

ENV SPRING_PROFILES_ACTIVE staging

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/urandom", "-jar", "/opt/app.jar" ]
CMD [""]

