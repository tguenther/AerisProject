
# Approach

I will take multiple passes at this.  My first pass will be simply an attempt to implement the API end points, launch the application, and successfully point a browser at the URL:port.  Next, I will refine the UI to allow for simple interaction with the end points.  Lastly I will refine and clean the code.

I plan to create several classes to implement this project.  There will be a class for main as required by java.  Additionally, I will create some helper classes to read the NetCDF data and containerize it.

For the Java, I will take the "Winebego" approach.  That is, everything will live in a single file at first.  Once I have a working solution, I will then clean it up and revise the implementation.

The UI will consist of a combintation of HTML and javascript.  Initially, there may be no simple navigation and each HTML file will be unlinked to the others.  That may change going forward during the revisions and cleaning phase.

This phased approach will allow me to learn as I go and ensure that I implement a minimal viable product.

# Design Decisions

# Implementation Details

# Development Notes

New PC Setup:
1. Installed necessary software

Lots of Learning:
1. Spring Boot framework
1. Java
1. RESTful API
1. NetCDF file format
1. Maven (build/deploy)

# Future enhancements

1. auto generate the html (jinja or similar?)
1. Create an animated gif of the time series data
1. linear interpolation across discrete time indexes to form a smooth transition
1. update image generation to include reference frame (i.e. lat/lon)

# Docker Container

The Dockerfile contains details to build the docker container.  Use the following command from the top level project folder to build the docker container:

`docker build -t aeris-project-app .`

Next, to run the container, use the following command:

`docker run -p 8080:8080 aeris-project-app`