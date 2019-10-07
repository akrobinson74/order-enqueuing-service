terraform {
  backend "s3" {
    bucket = "kotlin-terraform"
    key = "orderservices/info/terraform.tfstate"
    region = "eu-central-1"
  }
}

provider "aws" {
  region = "eu-central-1"
}

/**
* Imported Resources/Data
*/
data "aws_iam_role" "phizzard_service" {
//  arn = "arn:aws:iam::806353235757:role/phizzard-servicerole"
  name = "phizzard-servicerole"
}

data "aws_vpc" "vpc_phizzard" {
  id = "vpc-a1314dca"
}

data "aws_alb" "orderservices-elb" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:loadbalancer/app/orderservices-elb/dbd652bc3d0450b9"
}

data "aws_alb_listener" "oes-listener" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:listener/app/orderservices-elb/dbd652bc3d0450b9/a6f47c5ed9fd4e87"
}

data "aws_alb" "ops-orderservices-elb" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:loadbalancer/app/ops-orderservices-elb/078887ba0ba9e242"
}

data "aws_alb_target_group" "oes-dev-tg" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:targetgroup/oes-dev-tg/dd82a028c9f90137"
}

data "aws_alb_target_group" "ops-dev-tg" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:targetgroup/ops-dev-tg/6d8b925417415332"
}

data "aws_alb_target_group" "ops-stage-tg" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:targetgroup/ops-stage-tg/7173a6b2c5dd9a6c"
}

data "aws_alb_target_group" "ops-tg" {
  arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:targetgroup/ops-tg/6afd9550e5990247"
}

data "aws_route53_zone" "phizzard-route-53" {
  zone_id = "ZE42I9BMPFVSU"
}

data "aws_security_group" "order_services_sg" {
  id = "sg-0675d547eab997ae0"
}

data "aws_subnet" "euc-1a" {
  id = "subnet-120e2679"
}

data "aws_subnet" "euc-1b" {
  id = "subnet-5c8ee821"
}

data "aws_subnet" "euc-1c" {
  id = "subnet-2d89fa60"
}
/**
* Imported Resources/Data
*/