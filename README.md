# rpa-dg-doc-assembly-api
 A Restful API that facilitates the functioning of the doc-assembly web component, by proxying calls to external services, and aggregating backend calls

# Setup

#### To clone repo and prepare to pull containers:
```
git clone https://github.com/hmcts/dg-docassembly-api.git
cd dg-docassembly-api/
```

#### Clean and build the application:
```
./gradlew clean
./gradlew build
```

#### To run the application:

VPN connection is required
At the moment java version must be set to 17 as 21 is not supported for local setup by CFTLib
For local setup only, there is a need to set the port in main/resources/application.yaml to 8080
to avoid conflicting with CFTLib

```
az login
./gradlew bootWithCCD
```



### Running contract or pact tests:

You can run contract or pact tests as follows:
```
./gradlew clean
```

```
./gradlew contract
```

You can then publish your pact tests locally by first running the pact docker-compose:

```
docker-compose -f docker-pactbroker-compose.yml up
```

and then using it to publish your tests:

```
./gradlew pactPublish
```

### Swagger UI
To view our REST API go to http://{HOST}/swagger-ui/index.html
On local machine with server up and running, link to swagger is as below
> http://localhost:8080/swagger-ui/index.html
> if running on AAT, replace localhost with ingressHost data inside values.yaml class in the necessary component, making sure port number is also removed.

## API Endpoints
A list of our endpoints can be found here
> https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/dg-docassembly-api.json
