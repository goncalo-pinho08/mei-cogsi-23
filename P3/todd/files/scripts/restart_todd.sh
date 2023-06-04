if sudo service ToddService restart; then
    echo "ToddService Restarted Successfuly"
    exit 0
else
    echo "Can't Restart ToddService"
    exit 2
fi
