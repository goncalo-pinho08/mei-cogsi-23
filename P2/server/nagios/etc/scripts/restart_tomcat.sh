if sudo service tomcat9 restart; then
    echo "Tomcat Restarted Successfuly"
    exit 0
else
    echo "Can't Restart Tomcat"
    exit 2
fi
