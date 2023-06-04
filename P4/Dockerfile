FROM tronyx/nagios:latest

RUN mkdir -p /usr/local

RUN apt-get update -y && \
  apt-get install -y git && \
  apt-get install -y openjdk-8-jdk-headless sudo iproute2

#copy todd_app from the host machine to the container
COPY ./todd/todd_app /usr/local/todd

WORKDIR /usr/local/todd

RUN ./gradlew clean build

RUN cp ./build/libs/todd-1.0.1.jar /opt/nagios/libexec/todd.jar

COPY scripts/monitor/install_send_email.sh /tmp/install_send_email.sh

RUN chmod +x /tmp/install_send_email.sh && \
    /tmp/install_send_email.sh && \
    rm -rf /tmp/install_send_email.sh

