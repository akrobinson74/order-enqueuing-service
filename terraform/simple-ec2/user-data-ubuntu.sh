#!/usr/bin/env bash

# docker prerequisites
sudo apt update -y
sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common \
    openssl

# docker CE
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo apt-key fingerprint 0EBFCD88
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) \
    stable"
sudo apt update -y
sudo apt install -y docker-ce docker-ce-cli containerd.io

# aws
sudo apt install -y awscli

# logstash
sudo apt install -y default-jdk
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
sudo apt install -y apt-transport-https
echo "deb https://artifacts.elastic.co/packages/7.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-7.x.list
sudo apt update -y && sudo apt install -y logstash

# let ubuntu do docker :)
usermod -a -G docker ubuntu

# script to pull image and start docker
cat>/tmp/service-restart.sh<<"EOT"
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
--name=ops \
-p ${inbound_port}:${container_port} \
-d \
$ecr_url
EOT

chmod 755 /tmp/service-restart.sh
cd /tmp/ && ./service-restart.sh