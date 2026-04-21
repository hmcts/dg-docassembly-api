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
For local setup only, there is a need to set the port in main/resources/application.yaml to 8080
to avoid conflicting with CFTLib

Requires docker desktop running.

You need to be logged in to Azure and have access to the ACR registry to pull the necessary containers for the application to run.
If you are not logged in, you can do so by running `az login` in your terminal.
Followed by `az acr login --name hmctsprod` to log in to the ACR registry

```
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

To run the provider pact tests, first comment the broker configuration
in the BaseProviderTest and uncomment the pact folder configuration,
then run the below command to execute the provider pact tests locally.

```./gradlew providerContractTests```

### Swagger UI
To view our REST API go to http://{HOST}/swagger-ui/index.html
On local machine with server up and running, link to swagger is as below
> http://localhost:8080/swagger-ui/index.html
> if running on AAT, replace localhost with ingressHost data inside values.yaml class in the necessary component, making sure port number is also removed.

## API Endpoints
A list of our endpoints can be found here
> https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/dg-docassembly-api.json
