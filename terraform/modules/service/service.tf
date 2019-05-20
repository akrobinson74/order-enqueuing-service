#input
variable "api_version" {}

variable "env" {}

variable "aws_region" {}

variable "health_check_path" {}

variable "health_check_code" {
  default = "200"
}

variable "containerport" {}

variable "ecs_cluster_id" {}

variable "ecs_servicerole_arn" {}

variable "vpc_id" {}

variable "load_balancer_arn" {}

variable "host_header" {}

variable "instace_count" {
  default = 1
}

variable "route_priority" {
  default = 100
}

variable "service_http_port" {}

variable "docker_registry_host" {
  default = "806353235757.dkr.ecr.eu-central-1.amazonaws.com"
}

variable "docker_registry_key" {
  default = "order-enqueuing-service"
}

variable "mongo_orders_dbname" {
  default = "orders"
}

#config
provider "aws" {
  region = "${var.aws_region}"
}

resource "aws_cloudwatch_log_group" "ecs_test_order_proc_svc" {
  name              = "ecs/${var.env}/phizzard-${var.docker_registry_key}"
  retention_in_days = 1
}

/*
** TODOs:
**  - setup SQS queue
**  - setup Mongo cluster
**
*/
data "template_file" "service" {
  template = <<EOT
[
    {
      "name": "order-processing-service",
      "image": "${var.docker_registry_host}/${var.docker_registry_key}:$${version}",
      "essential": true,
      "memoryReservation": 512,
      "cpu": 512,
      "environment": [
        {
          "name": "APP_ENV",
          "value": "$${env}"
        },
        {
          "name": "CLOUDWATCH_AWS_KEY",
          "value": "AKIAJXPGVEZS37NE53ZA"
        },
        {
          "name": "CLOUDWATCH_AWS_REGION",
          "value": "$${region}"
        },
        {
          "name": "CLOUDWATCH_AWS_SECRET",
          "value": "IvWToFxgM285/yFbY0/m+xbTXOC1iUEFjZzlqSQc"
        },
        {
          "name": "CLOUDWATCH_AWS_TOKEN",
          "value": ""
        },
        {
          "name": "CLOUDWATCH_GROUP_NAME",
          "value": "ecs/$${env}/phizzard-kotlin"
        },
        {
          "name": "CLOUDWATCH_STREAM_NAME",
          "value": ""
        },
        {
          "name": "ELASTICSEARCH_URL",
          "value": "https://search-search-hpxggu3ndtubx7szxue4pcxb7a.eu-west-1.es.amazonaws.com/"
        },
        {
          "name": "MONGO_URL",
          "value: "mongodb+srv://phizzardSuperUser:0e6e2659027c05fb79665be23ca21793a583a65b5d44befbc0d00a4382c05669@orders0-mowjm.mongodb.net/${var.mongo_orders_dbname}?retryWrites=true"
        },
        {
          "name": "REDIS_URL",
          "value": "redis://phizzardapi.zymkgj.0001.euc1.cache.amazonaws.com:6379"
        },
        {
          "name": "SQS_URL",
          "value": "https://sqs.eu-central-1.amazonaws.com/806353235757/incoming-orders"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "ecs/$${env}/phizzard-${var.docker_registry_key}",
          "awslogs-region": "$${region}",
          "awslogs-stream-prefix": "$${version}"
        }
      }
    }
]
EOT

  vars {
    version       = "${var.api_version}"
    env           = "${var.env}"
    region        = "${var.aws_region}"
    containerport = "${var.containerport}"
  }
}

resource "aws_ecs_task_definition" "service" {
  family                = "order-enqueuing-service-staging"
  container_definitions = "${data.template_file.service.rendered}"
}

resource "aws_ecs_service" "service" {
  name                               = "${var.docker_registry_key}_${var.env}"
  cluster                            = "${var.ecs_cluster_id}"
  task_definition                    = "${aws_ecs_task_definition.service.arn}"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 200
  desired_count                      = "${var.instace_count}"
  iam_role                           = "${var.ecs_servicerole_arn}"
}
