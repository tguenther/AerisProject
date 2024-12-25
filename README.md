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
