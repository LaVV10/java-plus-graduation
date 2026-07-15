#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
# Canonical script: https://maven.apache.org/wrapper/
# ----------------------------------------------------------------------------

# Determine the project base directory
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  MAVEN_PROJECTBASEDIR=$(cd "$(dirname "$0")" && pwd)
  export MAVEN_PROJECTBASEDIR
fi

MAVEN_CMD_LINE_ARGS="$@"
MAVEN_OPTS=${MAVEN_OPTS:--Xmx512m}

# Determine the Java command to use to start the JVM
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD=$JAVA_HOME/jre/sh/java
  else
    JAVACMD=$JAVA_HOME/bin/java
  fi
  if [ ! -x "$JAVACMD" ] ; then
    echo "Error: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD=java
  command -v "$JAVACMD" >/dev/null 2>&1 || {
    echo "Error: 'java' not found and JAVA_HOME not set." >&2
    exit 1
  }
fi

# Resolve wrapper jar relative to project base dir
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

# Download the wrapper jar if it does not exist
if [ ! -e "$WRAPPER_JAR" ]; then
  wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
  if [ -f "$WRAPPER_PROPERTIES" ]; then
    # shellcheck disable=SC1090
    . "$WRAPPER_PROPERTIES"
  fi
  echo "Downloading Maven Wrapper from $wrapperUrl"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$wrapperUrl" -o "$WRAPPER_JAR" || exit 1
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$wrapperUrl" || exit 1
  else
    echo "Could not download Maven Wrapper. Please install curl or wget." >&2
    exit 1
  fi
fi

# Execute Maven using the wrapper jar
exec "$JAVACMD" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  $WRAPPER_LAUNCHER "$@"
