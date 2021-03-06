# Releases

## 2.0.0 (2022-04-09)
* view message count
* maximum loaded messages from pulsar
* from field default value hour before to

## 1.0.1 (2022-02-15)
* quarkus version upgrade

## 1.0.0 (2021-12-13)
* filtering with from and to
* version displayed in ui
* fix json path filtering with non json data in topic

## 0.3.0 (2021-12-06)
* filtering by message key
* improved performance when filtered by time

## 0.2.1 (2021-10-22)
* fix logging when using pulsar admin

## 0.2.0 (2021-10-05)
* filtering by topic pattern (possibility to add * at the end)
* selecting messages published within last x minutes
* reverse order of messages (the newest first)

New feature requires admin access. There are new properties to configure (PULSAR_ADMIN_SERVICE_HTTP_URL, PULSAR_ADMIN_TLS_CERT_FILE, PULSAR_ADMIN_TLS_KEY_FILE)

## 0.1.3 (2021-08-26)
* fix issue with none schema topics

## 0.1.2 (2021-08-21)
* fix issue when parsing invalid json via json path expression

## 0.1.1 (2021-07-27)
* better readiness health check with topic configuration

## 0.1.0 (2021-07-24)
* REST endpoint GET /read
* Simple html page
* Health checks
* Docker build
