#!/bin/bash

echo "Tomcat Deployment Script for Java Deprecation Utility"
echo "========================================================"

WAR_FILE="target/deprecation-utility.war"
TOMCAT_WEBAPPS="${TOMCAT_HOME:-/usr/local/tomcat}/webapps"
APP_NAME="deprecation-utility"

if [ ! -f "$WAR_FILE" ]; then
    echo "WAR file not found: $WAR_FILE"
    echo "   Please run 'mvn clean package' first"
    exit 1
fi

# Check if Tomcat webapps directory exists
if [ ! -d "$TOMCAT_WEBAPPS" ]; then
    echo "Tomcat webapps directory not found: $TOMCAT_WEBAPPS"
    echo "   Please set TOMCAT_HOME environment variable or update the script"
    echo "   Example: export TOMCAT_HOME=/path/to/your/tomcat"
    exit 1
fi

echo "Deploying $APP_NAME to Tomcat..."
echo "   Source: $WAR_FILE"
echo "   Destination: $TOMCAT_WEBAPPS/$APP_NAME.war"

echo "Stopping Tomcat..."
if pgrep -f tomcat > /dev/null; then
    pkill -f tomcat
    sleep 3
fi

if [ -d "$TOMCAT_WEBAPPS/$APP_NAME" ]; then
    echo "Removing existing deployment..."
    rm -rf "$TOMCAT_WEBAPPS/$APP_NAME"
fi

if [ -f "$TOMCAT_WEBAPPS/$APP_NAME.war" ]; then
    echo "Removing existing WAR file..."
    rm -f "$TOMCAT_WEBAPPS/$APP_NAME.war"
fi

echo "Copying WAR file..."
cp "$WAR_FILE" "$TOMCAT_WEBAPPS/"

echo "Starting Tomcat..."
cd "$TOMCAT_WEBAPPS/.."
./bin/startup.sh

echo "Waiting for deployment to complete..."
sleep 10

echo "Testing application..."
if curl -s "http://localhost:8080/$APP_NAME/" > /dev/null; then
    echo "Deployment successful!"
    echo "Application is available at: http://localhost:8080/$APP_NAME/"
    echo "Open your browser and navigate to the URL above"
else
    echo "Deployment failed or application not accessible"
    echo "Check Tomcat logs for errors: $TOMCAT_WEBAPPS/../logs/catalina.out"
fi

echo ""
echo "Useful commands:"
echo "   - View logs: tail -f $TOMCAT_WEBAPPS/../logs/catalina.out"
echo "   - Stop Tomcat: $TOMCAT_WEBAPPS/../bin/shutdown.sh"
echo "   - Start Tomcat: $TOMCAT_WEBAPPS/../bin/startup.sh"
echo "   - Access app: http://localhost:8080/$APP_NAME/"

