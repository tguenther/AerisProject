# Use Maven image to build the app
FROM maven:3.8.5-openjdk-17 AS build
# Copy the necessary files to the container
COPY src /home/app/src
COPY html /home/app/html
COPY pom.xml /home/app
COPY concentration.timeseries.nc /home/app
# Build the app
RUN mvn -f /home/app/pom.xml clean package

# Use the OpenJDK image to run the app
FROM openjdk:17
# Set the working folder to the app home directory
WORKDIR /home/app
# Copy the necessary run-time files
COPY --from=build /home/app/html ./html
COPY --from=build /home/app/concentration.timeseries.nc ./
# Copy the built jar file to the container
COPY --from=build /home/app/target/AerisProject-0.0.1-SNAPSHOT.jar /usr/local/lib/AerisProject.jar
# Expose the port the app will respond on
EXPOSE 8080
# Run the jar file
ENTRYPOINT ["java","-jar","/usr/local/lib/AerisProject.jar"]