Connected Vehicle Telemetry Service — Spring Boot + Kafka + Azure DevOps
A production-minded, VS Code–friendly starter that proves a clean Spring Boot 3.5 (Java 21) service producing to and consuming from Kafka, with a simple REST API and an Azure DevOps pipeline.
It is purposely small, opinionated, and ready for hands-on demonstrations (health checks, test suite, container image, local Kafka via Redpanda, and CI YAML).
What this project is for
Demonstrates an end-to-end, event-driven microservice pattern for connected-vehicle scenarios: a backend publishes telemetry events (e.g., vehicle speed) to Kafka and can read back recent events for quick verification.
Ideal for interview portfolios and proof-of-competence where you must show Spring Boot + Kafka + CI working together with clean code and tests.
Key capabilities
• Spring Boot 3.5 (Java 21) REST service with Actuator health. • Kafka integration (producer + consumer) with a toggleable No-Kafka mode. • Docker image and docker compose for local Redpanda. • Azure DevOps pipeline (YAML) with mvn verify and optional Docker build & push. • VS Code launch/tasks and editorconfig. • MockMvc tests for core endpoints.
Architecture overview
Endpoints: POST /api/telemetry/publish publishes telemetry JSON to Kafka; GET /api/telemetry/recent returns a recent ring buffer of consumed messages (Kafka mode only).
In No-Kafka mode the producer is a no-op and /recent returns an empty list; useful for fast local development.
Repository structure (excerpt)
src/main/java/io/example/telemetry/
TelemetryApplication.java
web/TelemetryController.java
service/ (KafkaTelemetryPublisher, NoopTelemetryPublisher, TelemetryConsumerService)
model/TelemetryMessage.java
config/KafkaConfig.java

src/test/java/io/example/telemetry/TelemetryApplicationTests.java

src/main/resources/application.yml
Dockerfile
compose.yaml
azure-pipelines.yml
.vscode/
Prerequisites
• Java 21 (Temurin) and Maven 3.9+ • Docker (for local Kafka/Redpanda) • VS Code with Java/Spring extensions
Quick start — No Kafka
# PowerShell
mvn spring-boot:run -Dspring-boot.run.arguments="--app.kafka.enabled=false --server.port=8080"
Invoke-WebRequest http://localhost:8080/actuator/health | Select-Object -ExpandProperty Content
curl -Method POST http://localhost:8080/api/telemetry/publish `
-Headers @{ "Content-Type"="application/json" } `
-Body '{"vehicleId":"WDB-TEST-001","speedKph":80.5}'
Run with Kafka (Redpanda)
docker compose up -d
$Env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"   # or: export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
mvn spring-boot:run
curl -X POST http://localhost:8080/api/telemetry/publish -H "Content-Type: application/json" `
-d '{"vehicleId":"WDB-TEST-001","speedKph":80.5,"gear":3}'
curl http://localhost:8080/api/telemetry/recent
Docker usage
mvn -q -DskipTests clean package
docker build -t connected-vehicle-telemetry:local .
docker run --rm -p 8080:8080 -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 connected-vehicle-telemetry:local
Azure DevOps CI/CD
# Stages
- Build & Test -> mvn verify
- Docker Build & Push (conditional) -> Docker@2 buildAndPush to ACR

# Variables
ACR_LOGIN_SERVER = <your-registry>.azurecr.io
DOCKER_REG_SVC_CONN = <service-connection-name>
API reference
• POST /api/telemetry/publish — Body: telemetry JSON (vehicleId, speedKph, ...). Returns 202 Accepted. • GET /api/telemetry/recent — JSON array of recently consumed messages (Kafka mode only). • GET /actuator/health — UP when healthy.
Testing
mvn -q test
# or
mvn -q -Dtest=TelemetryApplicationTests test
Tests verify: publish returns 202 with Kafka disabled; recent returns [] in No-Kafka mode; health returns UP.
Operational notes & troubleshooting
• Port in use: change via --server.port=8081. • PowerShell lifecycle phase errors: run 'mvn -Dspring-boot.run.arguments="--app.kafka.enabled=false --server.port=8080" spring-boot:run'. • Ensure KAFKA_BOOTSTRAP_SERVERS is reachable (localhost:9092 with Redpanda). • Prefer Invoke-WebRequest/Invoke-RestMethod over curl on Windows when needed.
Security & data
This demo publishes arbitrary JSON to a local developer Kafka broker and stores only a small in-memory ring buffer. Never use PII or production data. Add authn/z with Spring Security or an API gateway when required.
Extending the demo
• Add a React dashboard polling /api/telemetry/recent. • Add Micrometer + Prometheus/Grafana; add liveness/readiness for K8s. • Validate schemas via JSON Schema or Avro + Schema Registry. • Add spring-kafka-test with embedded broker. • Split into producer/consumer microservices and add a gateway.
License
MIT