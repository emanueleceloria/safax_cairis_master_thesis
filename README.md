# safax_cairis_master_thesis
Code developed to support integration between Cairis and Safax projects

Folder named "cairis" represents the cairis module implemented on Safax

In folder WEB-INF/classes it's possible to find all the .class files where the code for the implementation of the cairis module is present, in particular they are organized in 3 packages:

- /nl/tue/sec/cairis/db  :  

In this package 2 files are present:
- DBAbstraction.class  : implements all the connection methods in order to connect to the DB
- DBFns.class  : implements all the queries done towards DB

- /nl/tue/sec/cairis/util

In this package 4 files are present:
- CairisUtil.class  : implements the writeLog method (parameters and configuration properties to be able to write on the Account Activity                            section on Safax)
- DBJerseyConfig.class  :  configuration class for the DB
- DataUtil.class  : implements mainly JSON convertion methods
- LogUtil.class  : implements log methods

- /nl/tue/sec/cairis/ws

In this package 1 file is present:
- CairisService.class  : core part of the implementation, it's the part where cairis' udfs are received, elaborated, requests towards CAIRIS are performed in order to retrieve risk values and finally the risk itself is evaluated against the threshold in order to support the final authorization decision

For the implementation of the code I tried to be as much as I could coherent with the main implementation of Safax both from the organizational point of view and the coding point of view.

In the folder "db_test" is present a script to create a safax risk table that simulates the risks retrieved by CAIRIS and that can be used to test the cairis module if for example CAIRIS service is not available

In the folder "example_policies_requests" are present the risk policies and requests, each one tests a different cairis api
