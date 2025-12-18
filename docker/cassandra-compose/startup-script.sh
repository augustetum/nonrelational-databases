#!/bin/sh

# run cassandra
echo "Starting Cassandra..."
/opt/cassandra/bin/cassandra -R &

CASSANDRA_PID=$!
echo "Cassandra started with PID: $CASSANDRA_PID"

# wait for cassandra to start accepting connections on port 9042
echo "Waiting for Cassandra to be ready..."

MAX_TRIES=60
COUNT=0

until cqlsh -e "describe keyspaces" > /dev/null 2>&1 || [ $COUNT -eq $MAX_TRIES ]; do
  COUNT=$((COUNT + 1))
  echo "Attempt $COUNT/$MAX_TRIES: Cassandra not ready yet, waiting..."
  sleep 2
done

if [ $COUNT -eq $MAX_TRIES ]; then
  echo "ERROR: Cassandra failed to start after $MAX_TRIES attempts"
  exit 1
fi

echo "Cassandra is ready!"

# Verify system.local is accessible (this is what Debezium queries)
echo "Verifying system.local table access..."
MAX_TRIES=30
COUNT=0

until cqlsh -e "SELECT cluster_name FROM system.local" > /dev/null 2>&1 || [ $COUNT -eq $MAX_TRIES ]; do
  COUNT=$((COUNT + 1))
  echo "Attempt $COUNT/$MAX_TRIES: system.local not accessible yet, waiting..."
  sleep 2
done

if [ $COUNT -eq $MAX_TRIES ]; then
  echo "ERROR: system.local table not accessible after $MAX_TRIES attempts"
  exit 1
fi

echo "system.local is accessible!"

# Extra wait to ensure stability
echo "Waiting an additional 10 seconds for Cassandra to stabilize..."
sleep 10

# Test connection one more time before starting Debezium
echo "Final connection test..."
if ! cqlsh -e "SELECT cluster_name FROM system.local" > /dev/null 2>&1; then
  echo "ERROR: Final connection test failed!"
  exit 1
fi

echo "All checks passed! Starting Debezium connector..."

java -Dlog4j.debug -Dlog4j.configuration=file:$DEBEZIUM_HOME/log4j.properties \
  --add-exports java.base/jdk.internal.misc=ALL-UNNAMED \
  --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-exports java.base/sun.nio.ch=ALL-UNNAMED \
  --add-exports java.management.rmi/com.sun.jmx.remote.internal.rmi=ALL-UNNAMED \
  --add-exports java.rmi/sun.rmi.registry=ALL-UNNAMED \
  --add-exports java.rmi/sun.rmi.server=ALL-UNNAMED \
  --add-exports java.sql/java.sql=ALL-UNNAMED \
  --add-opens java.base/java.lang.module=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.loader=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.ref=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.reflect=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.math=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.module=ALL-UNNAMED \
  --add-opens java.base/jdk.internal.util.jar=ALL-UNNAMED \
  --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
  --add-opens=java.base/java.io=ALL-UNNAMED \
  -jar $DEBEZIUM_HOME/debezium-connector-cassandra.jar $DEBEZIUM_HOME/debezium-agent.properties