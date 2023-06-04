FROM "ubuntu:18.04"

LABEL __copyright__="(C) Guido Draheim, licensed under the EUPL" \
      __version__="1.5.7106"

EXPOSE 80
EXPOSE 8080

RUN apt-get update
RUN apt-get install -y tomcat9 python3 openssh-server sudo iproute2

COPY files/docker/systemctl3.py /usr/bin/systemctl
RUN test -L /bin/systemctl || ln -sf /usr/bin/systemctl /bin/systemctl

#tomcat had no permitiion to write to /var/lib/tomcat9
RUN chown -R tomcat:tomcat /var/lib/tomcat9

RUN systemctl enable tomcat9
RUN systemctl start ssh
RUN systemctl enable ssh

RUN mkdir -p /usr/local
WORKDIR /usr/local

#copy todd_app from the host machine to the container
COPY ./todd_app /usr/local/todd

WORKDIR /usr/local/todd

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

COPY files/scripts/restart_tomcat.sh /usr/local/nagios/libexec/restart_tomcat.sh

RUN sh -c 'echo "CATALINA_OPTS=\"-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=6000 -Dcom.sun.management.jmxremote.rmi.port=6000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=10.6.0.4\"" > /usr/share/tomcat9/bin/setenv.sh' && chmod 776 /usr/share/tomcat9/bin/setenv.sh


CMD ["/usr/bin/systemctl"]
