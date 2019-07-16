#!/usr/bin/env bash

sudo yum install -y aws-cli
sudo yum update -y
sudo yum -y install docker

sudo usermod -a -G docker ec2-user
sudo services docker start

## install java 8
sudo yum install -y java-1.8.0-openjdk

# script to pull image and start docker container
cat>/service-restart.sh<<EOT
#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID="${aws_key}"
export AWS_SECRET_ACCESS_KEY="${aws_secret}"

ecr_url="806353235757.dkr.ecr.eu-central-1.amazonaws.com/${project}:latest"

eval \$(aws ecr get-login --region eu-central-1 --no-include-email)
docker pull \$ecr_url
docker stop ${service}
docker rm ${service}
docker run \\
    -e JAVA_OPTS="-javaagent:/opt/newrelic.jar -Dnewrelic.config.license_key=${nr_license_key} -Dnewrelic.config.app_name=${app_name} -Dnewrelic.config.distributed_tracing.enabled=true" \\
    -e AWS_ACCESS_KEY_ID="${aws_key}" \\
    -e AWS_SECRET_ACCESS_KEY="${aws_secret}" \\
    -e NR_ACCOUNT_ID="${nr_account_id}" \\
    -e NR_INSIGHTS_KEY="${nr_license_key}" \\
    -e NEW_RELIC_APP_NAME="${app_name}" \\
    -e NEW_RELIC_LICENSE_KEY="${nr_license_key}" \\
    -e ENV=${deployment_tier} \\
    --name=${service} \\
    -p ${inbound_port}:${container_port} \\
    -d \$ecr_url

EOT

cd /
chmod 755 service-restart.sh
./service-restart.sh
