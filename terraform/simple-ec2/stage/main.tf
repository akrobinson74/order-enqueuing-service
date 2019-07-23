terraform {
  backend "s3" {
    bucket = "kotlin-terraform"
    key = "stage/orderservices/oes/terraform.tfstate"
    region = "eu-central-1"
  }
}

//variable "app_name" {}
variable "aws_access_key_id" {}
variable "aws_region" {
  default = "eu-central-1"
}
variable "aws_secret_access_key" {}
variable "cluster_name" {
  default = "order-services"
}
variable "deployment_tier" {
  default = "stage"
}
variable "inbound_port" {
  default = 80
}
# must be either "oes" or "ops"
variable "service" {
  default = "oes"
}
variable "nr_account_id" {}
variable "nr_license_key" {}
variable "route_53_zone_id" {
  default = "ZE42I9BMPFVSU"
}
variable "version-tag" {
  default = "stage"
}

provider "aws" {
  region = "eu-central-1"
}

data "aws_ami" "amazon-linux" {
  most_recent = true

  filter {
    name = "name"
    values = [
      "amzn-ami-*-ecs-optimized"]
  }

  owners = [
    "amazon"]
}

data "template_file" "user_data_oes" {
  template = "${file("${path.module}/../user-data-amazon-linux.sh")}"

  vars = {
    app_name = "OrderEnqueuingService"
    aws_key = "${var.aws_access_key_id}"
    aws_region = "${var.aws_region}"
    aws_secret = "${var.aws_secret_access_key}"
    cluster_name = "${var.cluster_name}"
    container_port = "9080"
    deployment_tier = "${var.deployment_tier}"
    inbound_port = "${var.inbound_port}"
    nr_account_id = "${var.nr_account_id}"
    nr_license_key = "${var.nr_license_key}"
    project = "order-enqueuing-service"
    service = "${var.service}"
    version = "${var.version-tag}"
  }
}

resource "aws_instance" "oes" {
  ami = "${data.aws_ami.amazon-linux.id}"
  associate_public_ip_address = true
  instance_type = "t2.micro"
  key_name = "akr-key-pair1"
  monitoring = true
  user_data = "${data.template_file.user_data_oes.rendered}"

  # order svcs security group: order_services_sg
  security_groups = [
    "sg-0675d547eab997ae0",
  ]

  # vpc phizzard subnet for eu-central-1a
  subnet_id = "subnet-120e2679"

  tags = {
    Name = "oes-${var.deployment_tier}"
  }
}

resource "aws_route53_record" "oes" {
  name = "oes${var.deployment_tier == "prod" ? "" : "-${var.deployment_tier}"}.phizzard.app"
  records = [
    "${aws_instance.oes.public_ip}"]
  ttl = 300
  type = "A"
  zone_id = "${var.route_53_zone_id}"
}

output "oes-public-ip" {
  value = "${aws_instance.oes.public_ip}"
}
