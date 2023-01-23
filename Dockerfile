# This Casino-app is not fully working using this dockerfile. This file serves as a remainder for tested commands.
# With this Dockerfile the Docker container's Tomee server's casino-app is not setting the context-root in server.xml file.
# Websockets in this app require the context root to be set, otherwise 404.
# Line 22 starting with "RUN sed.." tries to solve the missing context root problem in server.xml
# But seems that there is timing error with Tomee startup and the "sed" command. The "sed" command works ok after Tomee has started.
# And after restarting the container also websockets start to work in the container.

FROM maven:3.8.5-openjdk-17 AS build
RUN mkdir /app
COPY /codes/backend/casino /app
WORKDIR /app

#RUN mvn -f  /app/pom.xml clean package  # runs tests, is slow
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip

FROM tomee:9.0.0.RC1-jre17-alpine-webprofile
RUN apk update && \
    apk add nano

COPY --from=build /app/* /usr/local/tomee/webapps/   # Copies all, should copy only package

#RUN sed -i '138i \<Context docBase="casino" path="/casino" reloadable="true"/\>' /usr/local/tomee/conf/server.xml
#CMD ["catalina.sh", "start"]
EXPOSE 8080
