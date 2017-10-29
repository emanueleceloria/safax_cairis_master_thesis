#readme.txt

In CairisService.class are defined two methods to perform authentication on CAIRIS and retrieve the risk value:
- Simple authentication uses just the default session_id=test present in CAIRIS.
- Http Basic Authentication is also supported by CAIRIS and uses username and password both equal to 'test' for now. In the future clearly, username and password could be changed. You just need to change the value of USER and PWD final static parameters.

As it is now, the Http Basic Authentication method is the default one, but if for some reasons Simple authentication is preferable to be used, you just need to comment the code that is surrounded by 'HTTP BASIC AUTHENTICATION' comment string and 'End HTTP BASIC AUTHENTICATION' comment string, and uncomment the single line of code that follows the 'SIMPLE SESSION_ID AUTHENTICATION' comment string.