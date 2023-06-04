#!/bin/sh
SERVICE_NAME=ToddService
PATH_TO_JAR=/usr/local/todd/todd.jar
PID_PATH_NAME=/tmp/ToddService-pid
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -cp $PATH_TO_JAR -Dcom.sun.management.jmxremote.port=10500 -Dcom.sun.management.jmxremote.rmi.port=10500 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=10.6.0.3 -Dcom.sun.management.jmxremote.host=0.0.0.0 net.jnjmx.todd.Server 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup -cp $PATH_TO_JAR -Dcom.sun.management.jmxremote.port=10500 -Dcom.sun.management.jmxremote.rmi.port=10500 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=10.6.0.3 -Dcom.sun.management.jmxremote.host=0.0.0.0 net.jnjmx.todd.Server 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
