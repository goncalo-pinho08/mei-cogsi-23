FROM "ubuntu:18.04"

LABEL __copyright__="(C) Guido Draheim, licensed under the EUPL" \
      __version__="1.5.7106"

RUN apt-get update -y && \
  apt-get install -y git && \
  apt-get install -y openjdk-8-jdk-headless python3 sudo openssh-server iproute2

COPY files/docker/systemctl3.py /usr/bin/systemctl
RUN test -L /bin/systemctl || ln -sf /usr/bin/systemctl /bin/systemctl

RUN systemctl start ssh
RUN systemctl enable ssh

RUN mkdir -p /usr/local
WORKDIR /usr/local

#copy todd_app from the host machine to the container
COPY ./todd_app /usr/local/todd

WORKDIR /usr/local/todd

RUN ./gradlew clean build

RUN cp ./build/libs/todd-1.0.1.jar ./todd.jar

ADD files-todd-service/ToddService.service /etc/systemd/system/ToddService.service
RUN chmod +x+r /etc/systemd/system/ToddService.service
ADD files-todd-service/ToddService.sh /usr/local/todd/ToddService.sh
RUN chmod +x /usr/local/todd/ToddService.sh

#INSTALL NRPE
COPY files/plugins/install_nrpe.sh /tmp/install_nrpe.sh
RUN chmod +x /tmp/install_nrpe.sh && \
    /tmp/install_nrpe.sh && \
    rm -rf /tmp/install_nrpe.sh

#INSTALL SEND_NSCA
COPY files/plugins/install_send_nsca.sh /tmp/install_send_nsca.sh
RUN chmod +x /tmp/install_send_nsca.sh && \
    /tmp/install_send_nsca.sh && \
    rm -rf /tmp/install_send_nsca.sh

COPY files/scripts/restart_todd.sh /usr/local/nagios/libexec/restart_todd.sh
    
RUN systemctl enable ToddService

CMD ["/usr/bin/systemctl"]
