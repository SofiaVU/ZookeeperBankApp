#!/bin/bash

mkdir -p /tmp/zookeeper
rm -fr /tmp/zookeeper/*
cd /tmp/zookeeper
touch myid
echo ${ZOO_MY_ID} >> myid

echo "server.1=zoo1:2888:3888" >> /etc/hosts
echo "server.2=zoo2:2888:3888" >> /etc/hosts
echo "server.3=zoo3:2888:3888" >> /etc/hosts

cd /zookeeper-3.4.14/conf
mv ./zoo_sample.cfg zoo.cfg
echo "server.1=zoo1:2888:3888" >> zoo.cfg
echo "server.2=zoo2:2888:3888" >> zoo.cfg
echo "server.3=zoo3:2888:3888" >> zoo.cfg

echo "Starting the zookeeper"
./../bin/zkServer.sh start-foreground zoo.cfg
