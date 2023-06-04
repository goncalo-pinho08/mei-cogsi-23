if sudo systemctl restart tomcat9; then
    echo "Tomcat restarted successfully"
    exit 0
else
    echo "Can't restart Tomcat or Tomcat is not running"
    exit 2
fi
