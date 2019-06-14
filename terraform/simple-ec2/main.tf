terraform {
  backend "s3" {
    bucket = "kotlin-terraform-stage"
    key = "orderservices/terraform.tfstate"
    region = "eu-central-1"
  }
}

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

provider "aws" {
  region = "eu-central-1"
}

data "aws_ami" "amazon-linux" {
  most_recent = true

  filter {
    name   = "name"
    values = ["amzn-ami-*-ecs-optimized"]
  }

  owners = ["amazon"]
}

data "template_file" "user_data_oes" {
  template = "${file("${path.module}/user-data-amazon-linux.sh")}"

  vars = {
    aws_key = "${var.aws_access_key_id}"
    aws_region = "${var.aws_region}"
    aws_secret = "${var.aws_secret_access_key}"
    cluster_name = "${var.cluster_name}"
    container_port = "9080"
    deployment_tier = "${var.deployment_tier}"
    inbound_port = "${var.inbound_port}"
    nr_account_id = "${var.nr_account_id}"
    nr_license_key = "${var.nr_license_key}"
    service = "${var.service}"
  }
}

data "template_file" "user_data_ops" {
  template = "${file("${path.module}/user-data-amazon-linux.sh")}"

  vars = {
    aws_key = "${var.aws_access_key_id}"
    aws_region = "${var.aws_region}"
    aws_secret = "${var.aws_secret_access_key}"
    cluster_name = "${var.cluster_name}"
    container_port = "9081"
    deployment_tier = "${var.deployment_tier}"
    inbound_port = "443"
    nr_account_id = "${var.nr_account_id}"
    nr_license_key = "${var.nr_license_key}"
    service = "ops"
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
    "sg-0b2ac6867f9311863",
  ]

  subnet_id = "subnet-a904c8d2"

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
    "sg-0b2ac6867f9311863",
  ]

  subnet_id = "subnet-a904c8d2"

  tags = {
    Name = "ops-staging"
  }
}

output "oes-public-ip" {
  value = "${aws_instance.oes.public_ip}"
}

output "ops-public-ip" {
  value = "${aws_instance.ops.public_ip}"
}
