#!/usr/bin/env bash

sudo yum install -y aws-cli
sudo yum update -y
sudo yum -y install awslogs docker
sudo usermod -a -G docker ec2-user
sudo services docker start

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
#java_base_version="11"
#java_sub_version="0"
#java_base_build="3"
#
#java_version=$java_base_version + "u$java_sub_version"
#java_build="b$java_base_build"
#java_version_with_build="$java_version-$java_build"
#
#sudo yum install -y wget
#wget --no-cookies --header "Cookie: gpw_e24=xxx; oraclelicense=accept-securebackup-cookie;" "http://download.oracle.com/otn-pub/java/jdk/11u0-b3/jdk-11u0-b3-linux-x64.rpm"
#sudo rpm -i jdk-11.0.3_linux-x64.rpm

## install java 8
sudo yum install -y java-1.8.0-openjdk

## install logstash
sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
sudo cat>/etc/yum.repos.d/logstash.repo<<EOT
[logstash-7.x]
name=Elastic repository for 7.x packages
baseurl=https://artifacts.elastic.co/packages/7.x/yum
gpgcheck=1
gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
enabled=1
autorefresh=1
type=rpm-md
EOT
sudo yum -y install logstash

# script to pull image and start docker container
cat>~/service-restart.sh<<"EOT"
#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID="${aws_key}"
export AWS_SECRET_ACCESS_KEY="${aws_secret}"

project="order-processing-service"
app_name="OrderProcessingService"

if [ "${service}" = "oes" ]; then
    app_name="OrderEnqueuingService"
    project="order-enqueuing-service"
fi

ecr_url="806353235757.dkr.ecr.eu-central-1.amazonaws.com/$project:latest"

eval $(aws ecr get-login --region eu-central-1 --no-include-email)
docker pull $ecr_url
docker stop ${service}
docker rm ${service}
docker run \
-e JAVA_OPTS="-javaagent:/opt/newrelic.jar -Dnewrelic.config.license_key=${nr_license_key} -Dnewrelic.config.app_name=$app_name -Dnewrelic.config.distributed_tracing.enabled=true" \
-e AWS_ACCESS_KEY_ID="${aws_key}" \
-e AWS_SECRET_ACCESS_KEY="${aws_secret}" \
-e NR_ACCOUNT_ID="${nr_account_id}" \
-e NR_INSIGHTS_KEY="${nr_license_key}" \
-e NEW_RELIC_APP_NAME="$app_name" \
-e NEW_RELIC_LICENSE_KEY="${nr_license_key}" \
-e ENV=${deployment_tier} \
--name=${service} \
-p ${inbound_port}:${container_port} \
-d \
$ecr_url
EOT

chmod 755 ~/service-restart.sh
#sudo chown ec2-user ~/service-restart.sh
cd && ./service-restart.sh
