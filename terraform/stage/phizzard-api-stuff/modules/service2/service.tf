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

variable "rds_cluster_name" {}
variable "rds_dbname" {}
variable "rds_username" {}
variable "rds_password" {}

#config
provider "aws" {
  region = "${var.aws_region}"
}

data "aws_rds_cluster" "clusterName" {
  cluster_identifier = "${var.rds_cluster_name}"
}

resource "aws_cloudwatch_log_group" "ecs_testweb" {
  name              = "ecs/${var.env}/phizzardweb"
  retention_in_days = 1
}

resource "aws_cloudwatch_log_group" "ecs_testapp" {
  name              = "ecs/${var.env}/phizzardapp"
  retention_in_days = 1
}

data "template_file" "service" {
  template = <<EOT
[
    {
      "name": "app",
      "image": "806353235757.dkr.ecr.eu-central-1.amazonaws.com/phizzardapp:$${version}",
      "essential": true,
      "memoryReservation": 480,
      "cpu": 500,
      "environment": [
        {
          "name": "MAIN_DATABASE_URL",
          "value": "mysql://$${username}:$${password}@$${endpoint}:$${port}/$${dbname}"
        },
        {
          "name": "USER_DATABASE_URL",
          "value": "mysql://$${username}:$${password}@$${endpoint}:$${port}/$${dbname}"
        },
        {
          "name": "APP_ENV",
          "value": "$${env}"
        },
        {
          "name": "ELASTICSEARCH_URL",
          "value": "https://search-search-hpxggu3ndtubx7szxue4pcxb7a.eu-west-1.es.amazonaws.com/"
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
          "value": "ecs/$${env}/phizzard-api"
        },
        {
          "name": "CLOUDWATCH_STREAM_NAME",
          "value": "" 
        },
        {
          "name": "REDIS_URL",
          "value": "redis://phizzardapi.zymkgj.0001.euc1.cache.amazonaws.com:6379" 
        },
        {
          "name": "AWS_S3_KEY",
          "value": "AKIAJDCRV2UPXHN26QIA"
        },
        {
          "name": "AWS_S3_SECRET",
          "value": "VYlSRFozdp01NkBA9MFLqJDX2CaEgJ1YF4VDTa44"
        },
        {
          "name": "AWS_S3_REGION",
          "value": "eu-central-1"
        },
        {
          "name": "AWS_S3_VERSION",
          "value": "latest"
        },
        {
          "name": "AWS_S3_IMPORTFILES_BUCKETNAME",
          "value": "phizzardimport"
        },
        {
          "name": "AWS_S3_IMAGES_BUCKETNAME",
          "value": "phizzardimportimages"
        },
        {
          "name": "AWS_S3_API_DESCRIPTIONS_BUCKETNAME",
          "value": "phizzardimportimages"
        }
      ],
      "logConfiguration": { 
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "ecs/$${env}/phizzardapp",
          "awslogs-region": "$${region}",
          "awslogs-stream-prefix": "$${version}"
        }
      }
    },
    {
      "name": "web",
      "image": "806353235757.dkr.ecr.eu-central-1.amazonaws.com/phizzardweb:$${version}",
      "portMappings": [
        {
          "hostPort": 0,
          "protocol": "tcp",
          "containerPort": $${containerport}
        }
      ],
      "essential": true,
      "memoryReservation": 480,
      "cpu": 500,
      "logConfiguration": { 
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "ecs/$${env}/phizzardweb",
          "awslogs-region": "$${region}",
          "awslogs-stream-prefix": "$${version}"
        }
      },
      "links": [
        "app:app"
      ]
    }
]
EOT

  vars {
    version       = "${var.api_version}"
    env           = "${var.env}"
    region        = "${var.aws_region}"
    containerport = "${var.containerport}"
    endpoint      = "${data.aws_rds_cluster.clusterName.endpoint}"
    port          = "${data.aws_rds_cluster.clusterName.port}"
    dbname        = "${var.rds_dbname}"
    username      = "${var.rds_username}"
    password      = "${var.rds_password}"
  }
}

resource "aws_ecs_task_definition" "service" {
  family                = "phizzardapi_${var.env}"
  container_definitions = "${data.template_file.service.rendered}"
}

resource "aws_ecs_service" "service" {
  name                               = "phizzardapi_${var.env}"
  cluster                            = "${var.ecs_cluster_id}"
  task_definition                    = "${aws_ecs_task_definition.service.arn}"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 200
  desired_count                      = "${var.instace_count}"
  iam_role                           = "${var.ecs_servicerole_arn}"

  depends_on = ["aws_alb_target_group.service"]

  load_balancer {
    target_group_arn = "${aws_alb_target_group.service.id}"
    container_name   = "web"
    container_port   = "${var.containerport}"
  }
}

data "aws_lb" "test" {
  arn = "${var.load_balancer_arn}"
}

resource "aws_alb_target_group" "service" {
  name     = "${var.env}-alb-tg"
  port     = 80
  protocol = "HTTP"
  vpc_id   = "${var.vpc_id}"

  health_check {
    path    = "${var.health_check_path}"
    matcher = "${var.health_check_code}"
  }
}

data "aws_lb_listener" "listener" {
  load_balancer_arn = "${var.load_balancer_arn}"
  port              = 443
}

resource "aws_alb_listener_rule" "service" {
  listener_arn = "${data.aws_lb_listener.listener.arn}"
  priority     = "${var.route_priority}"

  action {
    type             = "forward"
    target_group_arn = "${aws_alb_target_group.service.arn}"
  }

  condition {
    field  = "host-header"
    values = ["${var.host_header}"]
  }
}
