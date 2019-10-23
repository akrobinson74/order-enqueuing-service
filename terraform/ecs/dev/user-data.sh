#!/bin/bash

yum install -y aws-cli
yum update -y
yum -y install ecs-init awslogs
echo ECS_CLUSTER=${cluster_name} >> /etc/ecs/ecs.config
echo ECS_LOGFILE=/var/log/ecs-agent.log >> /etc/ecs/ecs.config

mkdir -p /var/log/ecs
mkdir -p /var/awslogs/state
mkdir -p /etc/awslogs

INSTANCE_ID=$(curl 169.254.169.254/latest/meta-data/instance-id)
cat>/etc/awslogs/awslogs.conf<<EOT
[general]
state_file = /var/awslogs/state/agent-state

[ecs_agent_log]
file = /var/log/ecs/ecs-agent.log*
log_group_name = ecsAgent-phizzard
log_stream_name = $INSTANCE_ID
initial_position = start_of_file
datetime_format = %b %d %H:%M:%S
EOT
sed -i -e \"s/region = us-east-1/region = eu-central-1/g\" /etc/awslogs/awscli.conf

service docker restart && start ecs && service awslogs start