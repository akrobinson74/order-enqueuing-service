terraform {
  backend "s3" {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/lb/terraform.tfstate"
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

data "terraform_remote_state" "vpc" {
  backend = "s3"

  config {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/vpc/terraform.tfstate"
    region = "eu-central-1"
  }
}

resource "aws_alb" "service" {
  name = "alb-order-services"
  internal = false
  security_groups = ["${aws_security_group.ecs-elb.id}"]
  subnets = ["${aws_subnet.public.*.id}"]
  vpc_id = "${aws_vpc.order_services.id}"

  depends_on = ["data.aws_subnet_ids.vpc","data.terraform_remote_state.ecs"]
}

output "lb_arn" {
  value = "${aws_alb.service.arn}"
}