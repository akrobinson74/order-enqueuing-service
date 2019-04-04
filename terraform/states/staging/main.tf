terraform {
  required_version = ">= 0.11.7"

  backend "s3" {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/phizzardapp-staging/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "vpc" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/vpc/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "ecs" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/ecs/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "lb" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/lb/terraform.tfstate"
    region = "eu-central-1"
  }
}

provider "aws" {
  version = "~> 1.19"
  region  = "eu-central-1"
}

variable "version" {}

variable "service_http_port" {
  default = 9080
}

module "service" {
  source              = "../../modules/service"
  api_version         = "${var.version}"
  aws_region          = "eu-central-1"
  containerport       = "${var.service_http_port}"
  ecs_cluster_id      = "${data.terraform_remote_state.ecs.ecs-cluster-id}"
  ecs_servicerole_arn = "${data.terraform_remote_state.ecs.ecs-servicerole-arn}"
  env                 = "staging"
  health_check_path   = "/health"
  host_header         = "staging.phizzard.order-processing-service"
  instace_count       = 1
  load_balancer_arn   = "${data.terraform_remote_state.lb.lb_arn}"
  service_http_port   = "${var.service_http_port}"
  vpc_id              = "${data.terraform_remote_state.vpc.vpc_id}"
}
