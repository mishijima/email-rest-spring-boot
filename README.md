# Email REST API

## Description
This application acts as an abstraction between 2 email providers.
It supports a failover from one provider to the other by doing a simple health check on the providers.  

This application uses MailGun and SendGrid as its providers, please refer to for more information on their APIs

* https://sendgrid.com/docs/API_Reference/Web_API_v3/index.html
* https://documentation.mailgun.com/en/latest/user_manual.html#sending-via-api

## Tech stack
* Spring Boot 1.5.x
* Java 1.7 and above
* MySQL 5.x
* HSQLDB (for testing)
* Maven 3.x  



## Todo
* Send a notification to someone if it failed to connect to both providers
* Unit test for EmailService#save()
* Limit the number of requests the client can make
* Scheduler to go through the list of items in the 'queue' table  



## Architecture

### Layers
* Resource - Handles the request and response
* Service - Validates and processes the request and generates a response
* Repository - Stores to database

### Properties files
* application.properties - Default properties file
* application-local.properties - Local environment properties file. If you need to override some default properties then include them in this file
* application-test.properties - Test environment properties file
* mailgun-mail.properties - MailGun properties
* mailgun-mail-test.properties - MailGun properties for testing
* sendgrid-mail.properties - SendGrid properties
* sendgrid-mail-test.properties - SendGrid properties for testing  



## Process flow
1. Client sends a request to /api/emails
2. EmailResource#sendEmail() accepts the requests and calls EmailService#save()
3. EmailService#save() does the following
    * Validates the inputs and will throw BadRequestException if there's an error
    * Executes health check on the primary provider and if it fails it'll try the secondary provider. When both fails it will save the email into the 'queue' table so that it can try to resend it again
    * Creates a connection to an email provider
    * Constructs the request body according to the selected provider
    * If it gets a 'good' response from the provider then execute an 'Async' method to save the email to the 'history' table and return the response
    * If it gets a 'bad' response from the provider then execute an 'Async' method to save the email to the 'queue' table and return the response  



## Setup

### Mail providers
* Create an account with SendGrid and MailGun
* Take notes on the api url and key
* There are 4 mail properties, 2 are used in local/dev/prod environments where other 2 are used for testing.
    * Update http-api.url and http-api.key on mailgun and sendgrid properties
    * You can leave http-api.from empty because it's not being used at the moment
    

### DB
```sql
mysql> create database totoro_email; -- Create the new database
mysql> create user 'totorouser'@'localhost' identified by 'ghibli'; -- Creates the user
mysql> grant all on totoro_email.* to 'totorouser'@'localhost'; -- Gives all the privileges to the new user on the newly created database
```

### Update application-local.properties if needed

```properties
# Since this is local environment it's fine to leave it to create-drop but don't use it in production as it's going to drop data from the tables
spring.jpa.hibernate.ddl-auto=create-drop
# Change 'totoro_email' if you have a different database name
spring.datasource.url=jdbc:mysql://localhost:3306/totoro_email
# Change the username and password if needed 
spring.datasource.username=totorouser
spring.datasource.password=ghibli
```  



## How to run it from the command line

Once you have finished with the setup, you can execute the following command to run it. If you want to run the test you can remove '-Dmaven.test.skip=true' from the command 
```text
mvn clean package -Dmaven.test.skip=true && java -jar -Dspring.profiles.active=local target/email-rest-api-0.0.1-SNAPSHOT.jar
```

Once started you should see the following lines and can start using it
```text
s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
com.totoro.EmailApplication              : Started EmailApplication in 5.289 seconds (JVM running for 5.755)
```  



## Endpoints
#### Sending an email
A 'POST' request is used to send an email to one or more recipients. 'to', 'cc' and 'bcc' are optionals but at least one has to be set.

Request structure

* "from" - The sender in String - Mandatory
* "to" - An array of recipients in String - Optionals - Max 10 recipients
* "cc" - An array of recipients in String - Optionals- Max 10 recipients
* "bcc" - An array of recipients in String - Optionals - Max 10 recipients
* "subject" - The email subject - Mandatory
* "text" - The email body - Mandatory

Request example

```text
POST /api/emails HTTP/1.1
Content-Type: application/json;charset=UTF-8
Host: localhost
Content-Length: <xyz>

{
  "from": "whoami@example.org",
  "to": [
    "john@example.org"
  ],
  "cc": [
	"peter@example.org"
  ],
  "subject": "hello",
  "text": "this is my message from totoro email api"
}
```

Response example
```text
HTTP/1.1 201 Created
Content-Type: application/json;charset=UTF-8
Content-Length: <xyz>

{
	"message": "Your email has been sent",
	"timestamp": 1511240884934
}
```
