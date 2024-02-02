# jgetpass
Read user password values from eDirectory LDAP server.

This utility will read password values from a Novell eDirectory LDAP server.   Password retreival requires that Universal Password be enabled,
and that the bind user have supervisor permission to the user, and the explicit read password policy setting on the UP policy.

When the target is an LDAP filter, the output will be in CSV format where the columns are:

| Column 1 | Column 2 | Column 3  |
| --- | --- | --- | 
| LDAP DN | password | read error (if any) |


## Example with DN

Input:
```shell
java -jar getpass-0.0.0-executable.jar "cn=Heather Hitchcock,ou=bulk,o=data" -s='ldaps://127.0.0.1' -d='cn=admin,o=sa' -w='password'
```

Output:
```
password123
```

## Example with search

Input:
```shell
java -jar getpass-0.0.0-executable.jar "(objectclass=inetOrgPerson)" -s='ldaps://127.0.0.1' -d='cn=admin,o=sa' -w='password'
```

Output:
```
"cn=Weiping Woods,ou=bulk,o=data",,object has no password attribute: error -16049
"cn=Etty Saha,ou=bulk,o=data","!tCmbvRrP,64%k^",
"cn=Heather Hitchcock,ou=bulk,o=data",password123,
```


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
