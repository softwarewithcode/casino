# Casino is not fully working using this dockerfile. Only a list of tables are returned for restful calls.
# Websockets in this app require the context root to be set, otherwise 404.
# With this Dockerfile the Docker container's Tomee server's casino-app is not setting the context-root in server.xml file.
# Line 21 starting with "RUN sed.." tries to solve the missing context root problem in server.xml
# Seems that there is timing error with Tomee startup and the "sed" command. The "sed" command works ok after Tomee has started.

FROM maven:3.8.5-openjdk-17 AS build
RUN mkdir /app
COPY /codes/backend/casino /app
WORKDIR /app

#RUN mvn -f  /app/pom.xml clean package  # runs tests, is slow
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip

FROM tomee:9.0.0.RC1-jre17-alpine-webprofile
RUN apk update && \
    apk add nano

COPY --from=build /app/* /usr/local/tomee/webapps/

#RUN sed -i '138i \<Context docBase="casino" path="/casino" reloadable="true"/\>' /usr/local/tomee/conf/server.xml
#CMD ["catalina.sh", "start"]
EXPOSE 8080



#docker build -t casino .
