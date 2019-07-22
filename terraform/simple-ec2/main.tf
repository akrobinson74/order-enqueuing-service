terraform {
  backend "s3" {
    bucket = "kotlin-terraform"
    key = "stage/orderservices/terraform.tfstate"
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
  template = "${file("${path.module}/user-data-amazon-linux.sh")}"

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
  }
}

data "template_file" "user_data_ops" {
  template = "${file("${path.module}/user-data-amazon-linux.sh")}"

  vars = {
    app_name = "OrderProcessingService"
    aws_key = "${var.aws_access_key_id}"
    aws_region = "${var.aws_region}"
    aws_secret = "${var.aws_secret_access_key}"
    cluster_name = "${var.cluster_name}"
    container_port = "9081"
    deployment_tier = "${var.deployment_tier}"
    inbound_port = "${var.inbound_port}"
    nr_account_id = "${var.nr_account_id}"
    nr_license_key = "${var.nr_license_key}"
    project = "order-processing-service"
    service = "ops"
  }
}

resource "aws_security_group" "order_svcs" {
  name = "order_services_sg"
  description = "allow ssh to all; port 80 filtered"
  # VPC ID of the 'vpc phizzard' VPC
  vpc_id = "vpc-a1314dca"

  egress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port = 0
    protocol = "-1"
    to_port = 0
  }

  ingress {
    cidr_blocks = ["0.0.0.0/0"]
    from_port = 22
    protocol = "tcp"
    to_port = 22
  }

  ingress {
    cidr_blocks = [
      "2.204.225.55/32",
      "31.214.144.28/32",
      "77.64.169.152/32",
      "77.245.45.50/32",
      "88.65.1.69/32",
      "10.0.0.0/8",
      "18.0.0.0/8",
      "172.16.0.0/12",
      "192.168.0.0/16"
    ]
    from_port = 80
    protocol = "tcp"
    to_port = 80
  }
}

resource "aws_instance" "oes" {
  ami = "${data.aws_ami.amazon-linux.id}"
  associate_public_ip_address = true
  instance_type = "t2.micro"
  key_name = "akr-key-pair1"
  monitoring = true
  user_data = "${data.template_file.user_data_oes.rendered}"

  security_groups = [
    "${aws_security_group.order_svcs.id}",
  ]

  # vpc phizzard subnet for eu-central-1a
  subnet_id = "subnet-120e2679"

  tags = {
    Name = "oes-staging"
  }
}

resource "aws_instance" "ops" {
  ami = "${data.aws_ami.amazon-linux.id}"
  associate_public_ip_address = true
  instance_type = "t2.micro"
  key_name = "akr-key-pair1"
  monitoring = true
  user_data = "${data.template_file.user_data_ops.rendered}"

  security_groups = [
    "${aws_security_group.order_svcs.id}",
  ]

  subnet_id = "subnet-120e2679"

  tags = {
    Name = "ops-staging"
  }
}

resource "aws_route53_record" "oes" {
  name = "oes-${var.deployment_tier}.phizzard.app"
  records = ["${aws_instance.oes.public_ip}"]
  ttl = 300
  type = "A"
  zone_id = "${var.route_53_zone_id}"
}

resource "aws_route53_record" "ops" {
  name = "ops-${var.deployment_tier}.phizzard.app"
  records = ["${aws_instance.ops.public_ip}"]
  ttl = 300
  type = "A"
  zone_id = "${var.route_53_zone_id}"
}

output "oes-public-ip" {
  value = "${aws_instance.oes.public_ip}"
}

output "ops-public-ip" {
  value = "${aws_instance.ops.public_ip}"
}
