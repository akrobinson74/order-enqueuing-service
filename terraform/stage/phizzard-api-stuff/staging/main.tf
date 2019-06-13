terraform {
  required_version = ">= 0.11.7"

  backend "s3" {
    bucket = "terraform-phizzard"
    key    = "aws/phizzardapp-staging/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "vpc" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard"
    key    = "aws/vpc/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "ecs" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard"
    key    = "aws/ecs/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "lb" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard"
    key    = "aws/lb/terraform.tfstate"
    region = "eu-central-1"
  }
}

provider "aws" {
  version = "~> 1.19"
  region  = "eu-central-1"
}

variable "version" {}

module "service" {
  source              = "../../modules/service2"
  api_version         = "${var.version}"
  env                 = "staging"
  instace_count       = 2
  aws_region          = "eu-central-1"
  vpc_id              = "${data.terraform_remote_state.vpc.vpc_id}"
  health_check_path   = "/health.php"
  containerport       = "80"
  ecs_cluster_id      = "${data.terraform_remote_state.ecs.ecs-cluster-id}"
  ecs_servicerole_arn = "${data.terraform_remote_state.ecs.ecs-servicerole-arn}"
  host_header         = "staging.phizzard.app"
  load_balancer_arn   = "${data.terraform_remote_state.lb.lb_arn}"
  rds_cluster_name    = "phizzardmaindbcluster"
  rds_dbname          = "phizzard_api_staging"
  rds_username        = "phizzardapi"
  rds_password        = "xRQt8UsnrK"
}
