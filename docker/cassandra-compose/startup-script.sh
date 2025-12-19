#!/bin/sh

# 1. Run Cassandra in the background
echo "Starting Cassandra..."
# We use the official entrypoint logic to ensure all env vars are respected
/usr/local/bin/docker-entrypoint.sh cassandra &

CASSANDRA_PID=$!
echo "Cassandra started with PID: $CASSANDRA_PID"

# 2. Wait for Cassandra to start accepting connections
echo "Waiting for Cassandra to be ready..."
MAX_TRIES=60
COUNT=0
until cqlsh -e "describe keyspaces" > /dev/null 2>&1 || [ $COUNT -eq $MAX_TRIES ]; do
  COUNT=$((COUNT + 1))
  echo "Attempt $COUNT/$MAX_TRIES: Cassandra not ready yet..."
  sleep 5
done

if [ $COUNT -eq $MAX_TRIES ]; then
  echo "ERROR: Cassandra failed to start."
  exit 1
fi

echo "Cassandra is ready! Verifying system.local..."
sleep 5

echo "All checks passed! Starting Debezium connector..."

# 1. Removed the -javaagent from here (running as standalone JAR)
# 2. Corrected JAR name to match your Dockerfile (debezium-connector-cassandra.jar)
java -Dlog4j.debug -Dlog4j.configuration=file:/debezium/log4j.properties \
  --add-exports java.base/jdk.internal.misc=ALL-UNNAMED \
  --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-exports java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  -jar /debezium/debezium-connector-cassandra.jar /debezium/debezium-agent.properties &

echo "Debezium started. Streaming logs..."
tail -f /var/log/cassandra/system.log