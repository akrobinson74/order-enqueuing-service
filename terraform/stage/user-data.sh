#!/usr/bin/env bash

echo ECS_CLUSTER=${ecs_cluster_name} >> /etc/ecs/ecs.config

yum install -y aws-cli
yum update -y
yum -y install ecs-init awslogs

mkdir -p /var/log/ecs
mkdir -p /var/awslogs/state
mkdir -p /etc/awslogs

INSTANCE_ID=$(curl 169.254.169.254/latest/meta-data/instance-id)
cat>/etc/awslogs/awslogs.conf<<EOT
[general]
state_file = /var/awslogs/state/agent-state

[ecs_agent_log]
file = /var/log/ecs/ecs-agent.log*
log_group_name = ecsAgent-${cluster_name}
log_stream_name = $INSTANCE_ID
initial_position = start_of_file
datetime_format = %b %d %H:%M:%S
EOT
sed -i -e "s/region = us-east-1/region = ${aws_region}/g" /etc/awslogs/awscli.conf

## install java 11 lts
java_base_version="11"
java_sub_version="0"
java_base_build="3"

java_version="${java_base_version}u${java_sub_version}"
java_build="b${java_base_build}"
java_version_with_build="${java_version}-${java_build}"

wget --no-cookies --header "Cookie: gpw_e24=xxx; oraclelicense=accept-securebackup-cookie;" "http://download.oracle.com/otn-pub/java/jdk/${java_version_with_build}/jdk-${java_version}-linux-x64.rpm"
sudo rpm -i jdk-${java_version}-linux-x64.rpm

## install logstash
rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
cat>/etc/yum.repos.d/logstash.repo<<EOT
[logstash-7.x]
name=Elastic repository for 7.x packages
baseurl=https://artifacts.elastic.co/packages/7.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
EOT
yum -y install logstash

service docker restart && start ecs && service awslogs start
