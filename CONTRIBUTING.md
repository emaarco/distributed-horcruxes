# Contributing

Ideas, fixes and new examples are welcome. Open an issue to discuss larger changes before you start a pull request.

## Prerequisites

- JDK 21
- Docker or Podman
- Node.js 22 for the BPMN lint and the Bruno scenarios

## Repository layout

| Path        | Content                                                                          |
| ----------- | -------------------------------------------------------------------------------- |
| `examples/` | One Spring Boot module per pattern, each with its own BPMN model and README      |
| `stack/`    | Docker Compose stack: Camunda 8 orchestration cluster, PostgreSQL, Elasticsearch |
| `bruno/`    | End-to-end scenarios for the examples                                            |
| `assets/`   | Images used by the READMEs and CHALLENGES.md                                     |
| `talks/`    | Slides of talks about this repository                                            |

Each example follows a hexagonal architecture: adapters in `adapter/in` and `adapter/out`, use cases and transaction
boundaries in `application/service`, the domain model in `domain`.

## Build and test

```bash
./gradlew build                                   # all modules, including unit tests
./gradlew :examples:outbox-pattern:build          # one module
./gradlew test
```

## Infrastructure

```bash
docker compose -f stack/docker-compose.yml up -d
```

The stack runs the consolidated Camunda 8 orchestration cluster (Zeebe, Operate and Tasklist in one container) plus
PostgreSQL and Elasticsearch:

- Zeebe gRPC on `localhost:26500`, REST and Operate on `localhost:8080` (`demo` / `demo`)
- PostgreSQL on `localhost:5432`, database `example_database`, user `admin` / `admin`

The examples recreate their schema on every start (`ddl-auto: create`, or `create-drop` in outbox-pattern and
saga-pattern).

## Running an example

Run `ExampleApplication.kt` of a module from your IDE (`.run/` holds run configurations for
after-transaction, idempotency-pattern, outbox-pattern and saga-pattern) or use Gradle:

```bash
./gradlew :examples:<pattern-name>:bootRun
```

All examples bind port 8081, except `saga-pattern` on 8083, so run one at a time.

## BPMN models

Each module keeps its process model in `src/main/resources`: `inner-circle-membership.bpmn`, or
`inner-circle-membership-compensation.bpmn` for the saga example. The build generates a type-safe process API from it
with bpmn-to-code:

```bash
./gradlew generateBpmnModelApi
npm ci && npm run lint:bpmn
```

## End-to-end scenarios

The `bruno/` directory holds the scenario suite (Bruno CLI 4.0.0, poll helpers in `bruno/collection.bru`):

- `01-happy-path`: register, confirm, assert the membership is activated.
- `02-reject-confirmation`: register, reject, assert the membership is declined.
- `03-no-empty-spots`: fill the remaining spots, register once more, assert the rejection path.
- `04-saga-compensation`: the rejection against `saga-pattern`, asserting "Revoke claim" ran as compensation.

```bash
cd bruno
npx --yes @usebruno/cli@4.0.0 run . --env local --tags pattern -r   # any example on port 8081
npx --yes @usebruno/cli@4.0.0 run . --env saga --tags saga -r       # saga-pattern
```

The scenarios assert process state through the Camunda v2 REST API and poll instead of sleeping.

- **Restart the example before every run.** The spot capacity lives in memory.
- **Reset the stack when switching to or from `saga-pattern`** with `docker compose down -v`. Both process
  definitions start on the same message.

## Continuous integration

Every pull request to `main` runs the Gradle build, the BPMN lint and the Bruno suite against a fresh stack for each of
after-transaction, outbox-pattern, idempotency-pattern, combined-pattern and saga-pattern. The base scenario is broken
on purpose and stays out of the suite.

## Commits

Use [semantic commit messages](https://www.conventionalcommits.org/), for example `feat(outbox-pattern): …`.
