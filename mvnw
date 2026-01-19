#!/bin/sh
# Maven Wrapper script for Unix systems

# OS specific support
cygwin=false
darwin=false
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true ;;
  Darwin*) darwin=true ;;
esac

# Find project base directory
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
export MAVEN_PROJECTBASEDIR

# Use Java from JAVA_HOME or PATH
if [ -n "${JAVA_HOME}" ]; then
  JAVA_CMD="${JAVA_HOME}/bin/java"
else
  JAVA_CMD="java"
fi

# Maven wrapper JAR location
MAVEN_WRAPPER_JAR="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"

# Download wrapper if needed
if [ ! -f "${MAVEN_WRAPPER_JAR}" ]; then
  echo "Downloading Maven Wrapper..."
  WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  mkdir -p "${MAVEN_PROJECTBASEDIR}/.mvn/wrapper"
  curl -o "${MAVEN_WRAPPER_JAR}" "${WRAPPER_URL}" 2>/dev/null || wget -O "${MAVEN_WRAPPER_JAR}" "${WRAPPER_URL}"
fi

# Run Maven
exec "${JAVA_CMD}" \
  ${MAVEN_OPTS} \
  -classpath "${MAVEN_WRAPPER_JAR}" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
