
## About


The following are the features of **Form Runtime App** project.
* Enables users to deploy the Forms/components into form runtime database. The deployed forms/components are available for a user to select and associate to a user task in workflows and case models. Upon execution of workflows/case models, these forms are displayed to the user in camunda tasklist or case inbox.
* Provides the following REST APIs required to manage forms/components in runtime. 
    * ***Deploy*** - To deploy a form, created from form modeler
    * ***FetchForms*** - To fetch all the forms deployed in runtime, search for a form, pagination and sorting
    * ***FetchFormByFormId*** - To fetch a form based on form id/key
* Provides the following REST APIs required to manage components in runtime
    * ***Deploy*** - To deploy a component, created from Manage Components
    * ***FetchComponents*** - To fetch all the components deployed in runtime, search for a component, pagination and sorting
    * ***FetchComponentsByComponentId*** - To fetch a form based on component id/key
* Supports **versioning** of forms/components and provides REST APIs to fetch the versions of forms/components
* Records the **history/audit** of actions performed on forms/components

## REST API Documentation
Click [here](https://techsophysol.sharepoint.com/sites/TechsophyDeveloperNetwork/augment/SitePages/TP-APP-RUNTIME-FORM.aspx?Mode=Edit) for more details on API documentation

## Postman Collection
click [here](url) for postman collection to test the REST APIs

## Built With
This section contains list of frameworks,libraries and tools used to bootstrap this project.
- Java 11
- Spring boot 2.4.5
- Gradle 7.0 or newer
- Mongodb 4.4
- keycloak 11.0.2
- IntelliJ Idea


## Environment Variables

Set the below environment variables to bootstrap this project.

| Name | Example Value |
| ------ | ------ |
| GATEWAY_URI | https://api-gateway.techsophy.com |
| KEYCLOAK_URL_AUTH | https://keycloak.techsophy.com/auth | 
| TP_MODELER_APP_MONGO_URI | mongodb://localhost:27017/techsophy-platform (Setup replica set)|


Click [here](https://git.techsophy.com/techsophy-platform/tp-cloud-config/blob/dev/tp-app-runtime-form-dev.yaml)  to view all application properties.

## Getting Started
In order to start working on this project follow below steps:

### Prerequisites
- Download and install [JDK 11](https://www.oracle.com/in/java/technologies/javase/jdk11-archive-downloads.html)
- Download and install [IntelliJ Idea](https://www.jetbrains.com/idea/download/#section=linux)
- Download and Install [Keycloak](https://www.keycloak.org/archive/downloads-11.0.2.html) and set **KEYCLOAK_URL_AUTH**
- Download and install [MongoDB]() and setup [replicaset]().
- Run [tp-cloud-config-server](https://git.techsophy.com/techsophy-platform/tp-cloud-config-server/blob/dev/README.md)  
- Run [tp-api-gateway](https://git.techsophy.com/techsophy-platform/tp-api-gateway)
- Download and install Postman - Click [here](https://www.postman.com/downloads/) to test the REST APIs

### Configure Keycloak
Click [here](url) to configure keycloak

### Run
The following instructions are useful to run the projet.
- Open terminal and run following cammand.
- clone this git repo using below url
>git clone https://git.techsophy.com/techsophy-platform/tp-app-runtime-form
- Open the created folder in intellij idea
- Set the [environment variables] 
- Start the application

### ACL in formData
Sharepoint link for ACL(Form Security) Documentation  Click [ACL](https://techsophysol.sharepoint.com/sites/TechsophyDeveloperNetwork/augment/SitePages/Form-Security-in-Awgment.aspx?source=https%3A%2F%2Ftechsophysol.sharepoint.com%2Fsites%2FTechsophyDeveloperNetwork%2Faugment%2FSitePages%2FForms%2FByAuthor.aspx&OR=Teams-HL&CT=1675754361971)

### Test
Test the REST APIs by importing the postman collection from postman or [swagger ui](url).

## Acknowledgments

## Contributions

## License

## Contact


