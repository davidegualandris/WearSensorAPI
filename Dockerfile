# we are extending everything from tomcat:8.0 image ...
FROM tomcat:8.0-jre8

MAINTAINER Gualandris_Davide

# COPY path-to-your-application-war path-to-webapps-in-docker-tomcat
COPY WearSensorAPI/target/WearSensorAPI.war /usr/local/tomcat/webapps/
