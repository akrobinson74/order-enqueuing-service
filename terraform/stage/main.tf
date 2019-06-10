terraform {
  backend "s3" {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/order-services-stage/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "ecs" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key = "aws/lb/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "lb" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key = "aws/lb/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "vpc" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key = "aws/vpc/terraform.tfstate"
    region = "eu-central-1"
  }
}

variable "ecs_cluster_id" {}

variable "ecs_servicerole_arn" {}

variable "env" {}

variable "health_check_path" {
  default = "/health"
}

variable "health_check_code" {
  default = "200"
}

variable "host_header" {}

variable "instace_count" {
  default = 1
}

variable "oes-version" {
  default = "latest"
}

variable "ops-version" {
  default = "latest"
}

variable "route_priority" {
  default = 100
}

variable "version" {}

data "template_file" "service" {
  template = <<EOD
[
  {
    "cpu": 128,
    "environment": [
      {
        "name": "MONGO_URL",
        "value": "mongodb+srv://phizzardTestDBUser:b55dd2655323fcbcb9df06c55af4bc51676964d2c294e0ac8628e1ac9d99060d@orders0-mowjm.mongodb.net/test_orders?ssl=true&sslInvalidHostNameAllowed=true&streamType=netty"
      },
      {
        "name": "SQS_URL",
        "value": "https://sqs.eu-central-1.amazonaws.com/806353235757/test-orders"
      },
      {
        "name": "PMA_ABSOLUTE_URI",
        "value": "${hostname}"
      }
    ],
    "essential": true,
    "image": "806353235757.dkr.ecr.eu-central-1.amazonaws.com/order-enqueuing-service:$${oes-version}",
    "memory": 128,
    "name": "order-enqueuing-service",
    "portMappings": [
      {
        "hostPort": 9080,
        "containerPort": 9080,
        "protocol": "tcp"
      }
    ]
  },
  {
    "cpu": 128,
    "environment": [
      {
        "name": "ORDER_SERVICE_URL",
        "value": "http://oes:9080"
      },
      {
        "name": "PMA_ABSOLUTE_URI",
        "value": "${hostname}"
      }
    ],
    "essential": true,
    "image": "806353235757.dkr.ecr.eu-central-1.amazonaws.com/order-processing-service:$${ops-version}",
    "links": [
      "order-enqueuing-service:oes"
    ],
    "memory": 128,
    "name": "order-processing-service",
    "portMappings": [
      {
        "hostPort": 9081,
        "containerPort": 9081,
        "protocol": "tcp"
      }
    ]
  }
]
EOD
  vars {
    oes-version = "${var.oes-version}"
    ops-version = "${var.ops-version}"
  }
}

resource "aws_ecs_task_definition" "service" {
  family                = "order-enqueuing-service-staging"
  container_definitions = "${data.template_file.service.rendered}"
}

resource "aws_ecs_service" "service" {
  name                               = "order-services-${var.env}"
  cluster                            = "${var.ecs_cluster_id}"
  task_definition                    = "${aws_ecs_task_definition.service.arn}"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 200
  desired_count                      = "${var.instace_count}"
  iam_role                           = "${var.ecs_servicerole_arn}"

  depends_on = ["aws_alb_target_group.service"]

  load_balancer {
    target_group_arn = "${aws_alb_target_group.service.id}"
    container_name   = "order-enqueuing-service"
    container_port   = "9080"
  }
}

data "aws_lb" "test" {
  arn = "${data.terraform_remote_state.lb.lb_arn}"
}

resource "aws_alb_target_group" "service" {
  name     = "${var.env}-alb-tg"
  port     = 9080
  protocol = "HTTP"
  vpc_id   = "${aws_vpc.order_services.id}"

  health_check {
    path    = "${var.health_check_path}"
    matcher = "${var.health_check_code}"
  }
}

data "aws_lb_listener" "listener" {
  load_balancer_arn = "${data.terraform_remote_state.lb.lb_arn}"
  port              = 9080
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