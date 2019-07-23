#!/usr/bin/env bash

sudo yum install -y aws-cli
sudo yum update -y
sudo yum -y install docker

sudo usermod -a -G docker ec2-user
sudo services docker start

## install java 8
sudo yum install -y java-1.8.0-openjdk

## install logstash
rpm -import https://artifacts.elastic.co/GPG-KEY-elasticsearch
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
sudo yum install -y logstash
usermod -a -G logstash ec2-user
# setup logstash config file
cat>/etc/logstash/conf.d/logstash.conf<<EOT
input {
    file {
        path => "/var/log/logback*.log"
        codec => "json"
        type => "logback"
    }
}

output {
    if [type]=="logback" {
         elasticsearch {
             hosts => [ "https://search-search-hpxggu3ndtubx7szxue4pcxb7a.eu-west-1.es.amazonaws.com:443" ]
             index => "${service}-logback-%%{+YYYY.MM.dd}"
        }
    }
}
EOT
# sudo -E bin/logstash-plugin install logstash-output-amazon_es

# script to pull image and start docker container
cat>/service-restart.sh<<EOT
#!/usr/bin/env bash

export AWS_ACCESS_KEY_ID="${aws_key}"
export AWS_SECRET_ACCESS_KEY="${aws_secret}"

ecr_url="806353235757.dkr.ecr.eu-central-1.amazonaws.com/${project}:${deployment_tier}"

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
    -e TEST_EMAIL=true \\
    -e TEST_MODE=true \\
    -e USE_FALLBACK_EMAIL=true \\
    --name=${service} \\
    -p ${inbound_port}:${container_port} \\
    -d \$ecr_url

EOT

cd /
chmod 755 service-restart.sh
./service-restart.sh
