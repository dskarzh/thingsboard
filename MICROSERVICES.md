# ThingsBoard Microservices Architecture

## Overview

ThingsBoard supports two deployment modes:

1. **Monolith** — All components run in a single JVM process (`ThingsboardServerApplication`). Uses in-memory queuing via `InMemoryMonolithQueueFactory`.
2. **Microservices** — Components are split into independently deployable services communicating over a message queue (Kafka by default). Orchestrated via Docker Compose or Kubernetes.

## Service Types

Defined in `common/message/.../ServiceType.java`:

| Service            | Role                                                             |
| ------------------ | ---------------------------------------------------------------- |
| **TB_CORE**        | Device management, REST API, WebSocket subscriptions, DB writes  |
| **TB_RULE_ENGINE** | Executes rule chains (message routing, transformations, actions)  |
| **TB_TRANSPORT**   | Protocol-specific device communication (MQTT, HTTP, CoAP, etc.)  |
| **JS_EXECUTOR**    | Remote execution of JavaScript rule nodes                        |
| **TB_VC_EXECUTOR** | Git-based version control synchronization                        |
| **EDQS**           | Entity Data Query Service for optimized data reads               |

## Inter-Service Communication

All services communicate through a pluggable message queue abstraction (`common/queue/`), selected by the `TB_QUEUE_TYPE` environment variable:

- **Kafka** (production default)
- **RabbitMQ** (alternative broker)
- **Google Cloud Pub/Sub** (cloud-native option)
- **In-Memory** (monolith/dev mode only)

Messages are defined as Protocol Buffers in `common/proto/src/main/proto/queue.proto` and `transport.proto`.

### Key Message Types

- `ToRuleEngineMsg` — device telemetry/events from transport to rule engine
- `ToCoreMsg` — rule engine results flowing back to core
- `ToTransportMsg` — server-side commands pushed to transport services
- `TransportApiRequestMsg` / `TransportApiResponseMsg` — device credential lookups

### Typical Message Flow

```
Device → Transport Service → [Kafka: ToRuleEngineMsg] → Rule Engine
                                                            ↓
                                              [Kafka: ToCoreMsg] → Core Service
                                                                      ↓
                                                              DB writes, notifications,
                                                              alarms, RPC responses
```

## Service Discovery

Production deployments use ZooKeeper via `ZkDiscoveryService`:

- Each service registers itself in ZooKeeper on startup
- ZooKeeper coordinates partition assignment for rule engine instances
- Rule chains are distributed across rule engine nodes for load balancing
- `DummyDiscoveryService` is used in standalone/monolith mode

## Transport Layer

Each protocol runs as an independent microservice:

| Protocol | Directory          | Default Port  |
| -------- | ------------------ | ------------- |
| MQTT     | `transport/mqtt/`  | 1883          |
| HTTP     | `transport/http/`  | 8080          |
| CoAP     | `transport/coap/`  | 5683/UDP      |
| LwM2M    | `transport/lwm2m/` | 5685-5686/UDP |
| SNMP     | `transport/snmp/`  | 1620/UDP      |

Each transport service:
1. Accepts device connections on its protocol
2. Validates credentials via `TransportApiRequestMsg` to Core
3. Forwards telemetry/attributes to the rule engine via Kafka
4. Receives server-to-device commands via `ToTransportMsg`

## Rule Engine in Microservices Mode

- Consumes from Kafka rule engine topics
- Executes rule chains (chains of `TbNode` implementations)
- JavaScript nodes delegate to the JS Executor service via request/response over Kafka
- Work is partitioned across multiple rule engine instances, coordinated by ZooKeeper
- Results are published back to the core topic

## Docker Compose Topology

From `docker/docker-compose.yml`:

```
HAProxy (LB) ─── tb-core1, tb-core2
              ├── tb-rule-engine1, tb-rule-engine2
              ├── tb-mqtt-transport1, tb-mqtt-transport2
              ├── tb-http-transport1, tb-http-transport2
              ├── tb-coap-transport
              ├── tb-lwm2m-transport
              ├── tb-snmp-transport
              ├── tb-js-executor (×10 replicas)
              ├── tb-vc-executor1, tb-vc-executor2
              ├── tb-web-ui1, tb-web-ui2
              ├── kafka
              └── zookeeper
```

## Key Configuration

| Variable             | Purpose                                      |
| -------------------- | -------------------------------------------- |
| `TB_SERVICE_TYPE`    | `tb-core`, `tb-rule-engine`, etc.            |
| `TB_QUEUE_TYPE`      | `kafka`, `rabbitmq`, `pubsub`, `in_memory`   |
| `TB_KAFKA_SERVERS`   | Kafka bootstrap servers                      |
| `ZOOKEEPER_ENABLED`  | Enable/disable service discovery             |
| `CACHE_TYPE`         | `redis` (distributed) or `caffeine` (local)  |

## Directory Structure

```
application/          → Core + monolith entry point
common/
  queue/              → Queue abstraction (Kafka, RabbitMQ, Pub/Sub, in-memory)
  transport/          → Transport API interfaces
  proto/              → Protobuf message definitions
  discovery-api/      → Service discovery interfaces
transport/            → Protocol implementations (mqtt, http, coap, lwm2m, snmp)
rule-engine/          → Rule chain processing engine
msa/                  → Microservice packaging & Docker image builds
  tb-node/            → Core/rule-engine Docker image
  transport/          → Transport Docker images
  js-executor/        → JS executor service
  vc-executor/        → Version control executor
  web-ui/             → Frontend service
docker/               → Docker Compose files, HAProxy config, monitoring
```
