# jgetpass
Read user password values from eDirectory LDAP server

## Build

Build pre-requisites:
* Java 11+
* Git
* The build uses maven, but you do not need to install it; the maven wrapper in the source tree will download a local version.

Build steps:
1. Set _JAVA_HOME_ environment variable to JDK home.
1. Clone the git project
1. Change to pwm directory
1. Run the maven build

Linux example:
```
export JAVA_HOME="/home/vm/JavaJDKDirectory"
git clone https://github.com/jrivard/jgetpass
cd jgetpass
./mvnw clean verify
```  
Windows example:
```
set JAVA_HOME="c:\JavaJDKDirectory" 
git clone https://github.com/jrivard/jgetpass
cd jgetpass
mvnw.cmd clean verify
```
On Windows we recommend using paths without spaces for both PWM and JDK directory.

The output build file will be in ```target/jgetpass-0.0.0-executable.jar```

## Execute

Execute pre-requisites:
* Java 1.8+

```java -jar jgetpass-0.0.0-executable.jar```
