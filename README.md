# disa-returns-api-tests

This repository contains the end-to-end API test suite for the [ISA Returns API](https://github.com/hmrc/disa-returns). It verifies monthly return submission, declaration and reconciliation results across supported environments.

## Pre-requisites

### Services

Start Mongo Docker container as follows:

```bash
docker run --rm -d -p 27017:27017 --name mongo percona/percona-server-mongodb:6.0
```

Start `DISA_RETURNS_ALL` services as follows:

```bash
sm2 --start PUSH_PULL_NOTIFICATIONS_API --appendArgs '{"PUSH_PULL_NOTIFICATIONS_API": ["-Dallowlisted.useragents.0=api-subscription-fields","-Dallowlisted.useragents.1=disa-returns","-DvalidateHttpsCallbackUrl=false"]}'


```

## Tests

Run tests as follows:

* Argument `<environment>` must be `local`, `dev`, `qa` or `staging`.

```bash
./run-tests.sh <environment>
```

## Scalafmt

Check all project files are formatted as expected as follows:

```bash
sbt scalafmtCheckAll
```

Format `*.sbt` and `project/*.scala` files as follows:

```bash
sbt scalafmtSbt
```

Format all project files as follows:

```bash
sbt scalafmtAll
```

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
