#!/usr/bin/env bash

BROKER=$(echo $BROKER_ID | cut -d '-' -f 2)

cd /opt/kafka/config
sed -i "s|broker.id=0|broker.id=${BROKER}|" server.properties
sed -i "s|zookeeper.connect=localhost:2181|zookeeper.connect=192.168.100.100:32364|" server.properties

cd /opt/kafka/bin
sed -i 's/KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false "/KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false  -Dcom.sun.management.jmxremote.ssl=false -javaagent:${base_dir}\/agent\/jmx_prometheus_javaagent-0.19.0.jar=12345:${base_dir}\/agent\/kafka_broker.yml"/' kafka-run-class.sh

cd /opt/kafka
bin/kafka-server-start.sh config/server.properties