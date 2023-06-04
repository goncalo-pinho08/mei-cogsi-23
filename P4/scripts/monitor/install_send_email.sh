#!/bin/sh
cd /tmp
#commands to install sendEmail
wget http://caspian.dotconf.net/menu/Software/SendEmail/sendEmail-v1.56.tar.gz
tar xzf sendEmail-v1.56.tar.gz
cp -a sendEmail-v1.56/sendEmail /usr/local/bin
chmod +x /usr/local/bin/sendEmail