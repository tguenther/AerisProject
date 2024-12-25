# AerisProject

The Aeris project application contains several endpoints for access to and display of NetCDF files.
Specifically, this application querires the file concentration.timeseries.nc for detailed
information, concentration data, and a visual representation of the concentration data.
Additional future enhancements may allow access to arbitrary files.  But, this is currently
not available as this is a prototype / demostration project.

This document describes the Aeris project.  Specifically, this document contains the high level requirements, 
a description of the implementation approach, and some design notes.  Additionally, the simplified API is also
included along with details about the simple UI.

# Requirements

1. Publicly hosted repository of your choice (GitHub, bitbucket, etc) 
2. The use of Spring Boot REST framework 
3. Implementation of the following endpoints 
    - /get-info, returns the NetCDF detailed information. 
    - /get-data, params to include time index and z index, returns json response that 
    includes x, y, and concentration data. 
    - /get-image, params to include time index and z index, returns png visualization of 
    concentration. 
4. Docker container deployment 
5. Project README.md 

# External API and Entry Points

**/**

Parameters: none

This is the default entry point and is accessible via the URL and port.
A web UI for accessing additional data may be accessed through this default entry point.  Se details under the [UI] section below.

Example: 

`http://localhost:8080/`

---

**/get-info**

Parameters:  none

This entry point takes no parameters and returns the NetCDF information from the server-size file: 

This entry point returns detailed information about the NetCDF file.  The information contains:
- title
- id
- file type
- file version
- class
- iosp
- file name
- file size
- variable listing

Example:

`http://localhost:8080/get-info`

---

**/get-data**

Parameters:
    timeIndex, integer
    zindex, integer

This entry point retrieves the time-indexed data at the requested time and Z indexes.  The data is returned in JSON format.

Example:

`http://localhost:8080/get-data?timeIndex=1&zIndex=0`

---

**/get-image**

Parameters:
    timeIndex, integer
    zindex, integer

This entry point renders an image of the data at the selected time and z indexes.
Example:

`http://localhost:8080/get-image?timeIndex=1&zIndex=0`

# UI Details

The default entry point presents the user with a UI capable of accessing the access points for retreiving data and images.

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