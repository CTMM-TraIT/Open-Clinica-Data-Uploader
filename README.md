# Open Clinica Data Uploader
[![codecov](https://codecov.io/gh/thehyve/Open-Clinica-Data-Uploader/branch/master/graph/badge.svg)](https://codecov.io/gh/thehyve/Open-Clinica-Data-Uploader)
[![Build Status](https://travis-ci.org/thehyve/Open-Clinica-Data-Uploader.svg?branch=master)](https://travis-ci.org/thehyve/Open-Clinica-Data-Uploader)

## Manual
This document contains the end-user and technical manuals of the OCDU-application (**O**pen **C**linica **D**ata **U**ploader). This application has been developed by [CTMM-TraIT](http://www.ctmm-trait.nl/) in cooperation with [the Hyve](http://http://thehyve.nl/) to facilitate data uploads to OpenClinica instances. OCDU supports 2 operation-modes: the interactive mode where end-users upload files to the web-application and errors are fixed interactively. The second mode is the unattended automated mode where the OCDU-application picks up tab-delimited input files and automatically uploads these to an OpenClinica-instance. 

## Releases
The compiled and released versions of OCDU will be made available for downloads from the CTMM-TraIT Github repository from the master branch. It can be found at: [https://github.com/CTMM-TraIT/Open-Clinica-Data-Uploader](https://github.com/CTMM-TraIT/Open-Clinica-Data-Uploader). Each release will have a set of accompanying configuration-files and manuals.

## User administration
Users can use OCDU with the same user-ID and password as the OpenClinica instance to which they want to upload data. To use ODCU, users must be authorized to use SOAP web-services in OpenClinica. Refer to the instructions in the [OpenClinica User Administration](https://docs.openclinica.com/3.1/administer-users#content-title-2979) manual on how to do this.

## Configuration files
This section details an example of a configuration setup where the OCDU configuration files are located in the Tomcat */conf* directory. To allow muptile applications to run on the same Tomcat instance it is advised to copy the two application configuration files application.yml and application.properties to the Tomcat configuration directory and to rename them to *application-ocdu.yml* and *application-ocdu.properties*.

Tomcat must now be started with two Java command-line options:

	-Dspring.config.location=${TOMCAT_HOME}/tomcat/conf/ -Dspring.profiles.active=ocdu

${TOMCAT_HOME} must be replaced by the directory where Tomcat is installed. 

Other configuration set-ups are possbile; details can be found on the [Spring website](http://docs.spring.io/spring-boot/docs/current/reference/html/howto-properties-and-configuration.html). 


Each of the configuration files contains a number of options to control the OCDU-application. Details on each configuration item can be found in the files themselves.

## Initial Deployment
The DB-user for the communication with the DB must be created when deploying the OCDU-application for the first time. This is done with the SQL-script called **prepare_db.sql**. When using the Postgres-DB it can be run with the command:
```
  psql postgres -f prepare_db.sql
```
After the first deployment Tomcat must be stopped and the property **spring.jpa.hibernate.ddl-auto** must be changed in the application-odcu.properties file to **update**. Note that the DB will be destroyed on any subsequent startup when using a value of **create** or **create-drop**. Therefore: do not use these values on a production DB-instance after the initial deployment !


##Build Instructions ##
The released WAR-files will be available on the CTMM-TraIT Git-hub repository. It is possbile to build the source yourself if you have [Gradle 2.3](https://gradle.org/) or newer installed. Version 2.3 has been verified to work. The command to build the OCDU-application are:
```
Windows: gradlew clean build test
Linux: gradle clean build test
```
The **test** option can be ommitted if you don't want to run the unit-tests.

##Unattended automated uploads