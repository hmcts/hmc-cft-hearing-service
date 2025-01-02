# hmc-cft-hearing-service

## Getting Started
Please note that this microservice is also available within [hmc-docker](https://github.com/hmcts/hmc-docker).

### Prerequisites

- [JDK 17](https://java.com)
- [Docker](https://www.docker.com)

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

The easiest way to run the application locally is to use the `bootWithCCD` Gradle task.

This task requires:
- Access to the `hmc-demo` key vault
- Two queues on the `hmc-servicesbus-demo` Azure Service Bus
- One topic on the `hmc-servicebus-demo` Azure Service Bus

See [Creating Azure Service Bus Resources](https://tools.hmcts.net/confluence/display/HMAN/Creating+Azure+Service+Bus+Resources) for detailed instructions on creating ASB resources.

**Set required environment variables**

The following environment variables need to be set for the created Azure Service Bus resources:

| Environment Variable | Description |
|----------------------|-------------|
| HMC_OUTBOUND_SERVICE_BUS_QUEUE | Outbound queue name |
| HMC_SERVICE_BUS_QUEUE | Inbound queue name |
| HMC_SERVICE_BUS_TOPIC | Topic name for publishing updates |

**Run the application**

Run the application by executing the following command:

```bash
./gradlew bootWithCCD
```

This will start the application and its dependent services.

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4561/health
```

You should get a response similar to this:

```
{"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative to running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/hmc-cft-hearing-service` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4561` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4561/health
```

You should get a response similar to this:

```
{"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

## Developing

### Unit tests

To run all unit tests execute the following command:
```bash
  ./gradlew test
```

### Integration tests

To run all integration tests execute the following command:
```bash
  ./gradlew integration
```
### Running contract or pact tests:

You can run and publish your pact tests locally by first running the pact docker-compose:

```
docker-compose -f docker-pactbroker-compose.yml up
```

And then using gradle task:

```
./gradlew runAndPublishConsumerPactTests
```
Run below command for the provider pact verification:

```
./gradlew clean runProviderPactVerification
```

Alternatively you can run single command for both consumer tests and provider verifications

```
./gradlew clean contract
```
### Code quality checks
We use [Checkstyle](http://checkstyle.sourceforge.net/).
To run all local checks execute the following command:

```bash
  ./gradlew check
```
## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

