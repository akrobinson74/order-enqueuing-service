#!/usr/bin/env bash

sudo yum install -y aws-cli
sudo yum update -y
sudo yum -y install docker

sudo usermod -a -G docker ec2-user
sudo services docker start

## install java 8
sudo yum install -y java-1.8.0-openjdk

#####################
## LOGSTASH SETUP
#####################
## install logstash
# mkdir common logging dir to be mounted by both containers
mkdir /var/log/logback
chmod 775 /var/log/logback
chown ec2-user:docker /var/log/logback
cat>/logstash-restart.sh<<EOT
#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID="${aws_key}"
export AWS_SECRET_ACCESS_KEY="${aws_secret}"

ecr_url="806353235757.dkr.ecr.eu-central-1.amazonaws.com/logstash-orderservices:oes-${deployment_tier}"

eval \$(aws ecr get-login --region eu-central-1 --no-include-email)
docker pull \$ecr_url
docker stop logstash
docker rm logstash
docker run \\
  --name logstash \\
  -v /var/log/logback:/var/log/logback \\
  -d \$ecr_url
EOT

#####################
## OPS SERVICE SETUP
#####################
# script to pull image and start docker container
cat>/service-restart.sh<<EOT
#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID="${aws_key}"
export AWS_SECRET_ACCESS_KEY="${aws_secret}"

tag=${version}
if [[ ! -z "\$1" ]]; then
    tag=\$1
fi

ecr_url="806353235757.dkr.ecr.eu-central-1.amazonaws.com/${project}:\$tag"

eval \$(aws ecr get-login --region eu-central-1 --no-include-email)
docker pull \$ecr_url
docker stop ${service}
docker rm ${service}
docker run \\
    -e JAVA_OPTS="-javaagent:/opt/newrelic/newrelic.jar -Dnewrelic.config.license_key=${nr_license_key} -Dnewrelic.config.app_name=${app_name} -Dnewrelic.config.distributed_tracing.enabled=true" \\
    -e AWS_ACCESS_KEY_ID="${aws_key}" \\
    -e AWS_SECRET_ACCESS_KEY="${aws_secret}" \\
    -e NR_ACCOUNT_ID="${nr_account_id}" \\
    -e NR_INSIGHTS_KEY="${nr_license_key}" \\
    -e NEW_RELIC_APP_NAME="${app_name}" \\
    -e NEW_RELIC_LICENSE_KEY="${nr_license_key}" \\
    -e NEW_RELIC_PROCESS_HOST_DISPLAY_NAME="oes${deployment_tier == "prod" ? "" : "-${deployment_tier}"}.phizzard.app" \\
    -e ENV=${deployment_tier} \\
    -e TEST_EMAIL=true \\
    -e TEST_MODE=true \\
    -e USE_FALLBACK_EMAIL=true \\
    --name=${service} \\
    -v /var/log/logback:/var/log/logback \\
    -p ${inbound_port}:${container_port} \\
    -d \$ecr_url

EOT

chmod 755 /logstash-restart.sh
chmod 755 /service-restart.sh
/service-restart.sh
/logstash-restart.sh
