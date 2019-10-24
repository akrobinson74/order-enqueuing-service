#!/bin/bash

# install aws-cli
yum install -y aws-cli
yum update -y

# make logging directories
mkdir -p /var/log/ecs
mkdir -p /var/awslogs/state
mkdir -p /etc/awslogs

# install awslogs and ecs-agent
yum -y install ecs-init awslogs

# create ecs config
echo ECS_CLUSTER=${cluster_name} >> /etc/ecs/ecs.config
echo ECS_LOGFILE=/var/log/ecs/ecs-agent.log >> /etc/ecs/ecs.config

# create awslogs config
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

# (re-)start necessary services
service docker restart && start ecs && service awslogs start