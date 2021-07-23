docker run -d --rm --name pulsar --net hpt \
-p 6650:6650 \
apachepulsar/pulsar:2.8.0 \
bin/pulsar standalone

docker run -d --rm --name source-app --net hpt -p 8081:8080 \
-e PULSAR_SERVICE_URL=pulsar://pulsar:6650 osomahe/pulsar-source-app:0.3.0