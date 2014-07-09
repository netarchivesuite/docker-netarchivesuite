#!/bin/sh

cd /root/quickstart-deploy
/root/quickstart-deploy/mq.sh start

su test -c "/home/test/QUICKSTART/conf/startall.sh"

/usr/sbin/sshd -D