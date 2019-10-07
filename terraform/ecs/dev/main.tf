terraform {
  backend "s3" {
    bucket = "kotlin-terraform"
    key = "dev/orderservices/oes/terraform.tfstate"
    region = "eu-central-1"
  }
}

data "terraform_remote_state" "info" {
  backend = "s3"

  config = {
    bucket = "kotlin-terraform"
    key = "orderservices/info/terraform.tfstate"
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
  default = "dev"
}
variable "inbound_port" {
  default = 80
}
# must be either "oes" or "ops"
variable "service" {
  default = "oes"
}
variable "nr_account_id" {}
variable "nr_insights_key" {}
variable "nr_license_key" {}
variable "route_53_zone_id" {
  default = "ZE42I9BMPFVSU"
}
variable "version-tag" {
  default = "dev"
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

resource "aws_ecs_cluster" "oes_cluster" {
  name = "order-enqueuing-service-cluster"
}

data "template_file" "user_data" {
  template = "${file("${path.module}/user-data.sh")}"
}

resource "aws_launch_configuration" "oes_dev_launch" {
  image_id = "${data.aws_ami.amazon-linux.id}"
  instance_type = "t2.small"
  name_prefix = "oes-launch-conf-"
  security_groups = [
    "${data.terraform_remote_state.info.outputs.order_services_sg}"]

  user_data = "${data.template_file.user_data.rendered}"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "oes_scaling" {
//  availability_zones = [
//    "eu-central-1a",
//    "eu-central-1b",
//    "eu-central-1c"]
  default_cooldown = "30"
  depends_on = ["aws_launch_configuration.oes_dev_launch"]
  health_check_grace_period = "120"
  health_check_type = "ELB"
  launch_configuration = "${aws_launch_configuration.oes_dev_launch.name}"
  max_size = 3
  min_size = 1
  name = "oes_autoscaling_group"
  vpc_zone_identifier = [
    "${data.terraform_remote_state.info.outputs.subnet-euc-1a}",
    "${data.terraform_remote_state.info.outputs.subnet-euc-1b}",
    "${data.terraform_remote_state.info.outputs.subnet-euc-1c}"]

  lifecycle {
    create_before_destroy = true
  }
}

data template_file "task_def" {
  template = "${file("${path.module}/task_definition.json")}"

  vars = {
    app_name = "OrderEnqueuingService"
    aws_key = "${var.aws_access_key_id}"
    aws_region = "${var.aws_region}"
    aws_secret = "${var.aws_secret_access_key}"
    cluster_name = "${var.cluster_name}"
    container_port = "80"
    containerPort = 80
    deployment_tier = "${var.deployment_tier}"
    env = "dev"
    inbound_port = "${var.inbound_port}"
    nr_account_id = "${var.nr_account_id}"
    nr_insights_key = "${var.nr_insights_key}"
    nr_license_key = "${var.nr_license_key}"
    project = "order-enqueuing-service"
    service = "${var.service}"
    tag = "latest"
    version = "${var.version-tag}"
  }
}

resource "aws_cloudwatch_log_group" "ecs_oes" {
  name = "ecs/${var.deployment_tier}/oes"
  retention_in_days = 1
}

resource "aws_ecs_task_definition" "oes_task_def" {
  container_definitions = "${data.template_file.task_def.rendered}"
  family = "oes_${var.deployment_tier}"
}

# imported by arn
resource "aws_alb_target_group" "oes-dev-tg" {
  port = 80
  protocol = "HTTP"
  vpc_id = "vpc-a1314dca"

  health_check {
    path = "/health"
    matcher = "200"
  }
}

resource "aws_alb_listener" "oes_dev_listener" {
  default_action {
    type = "forward"
    target_group_arn = "${aws_alb_target_group.oes-dev-tg.arn}"
  }

  load_balancer_arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:loadbalancer/app/orderservices-elb/dbd652bc3d0450b9"
  port = 443
}

resource "aws_ecs_service" "oes_service" {
  cluster = "${aws_ecs_cluster.oes_cluster.id}"
  deployment_maximum_percent = 200
  deployment_minimum_healthy_percent = 50
  desired_count = 1
  iam_role = "arn:aws:iam::806353235757:role/phizzard-servicerole"
  name = "oes-ecs-${var.deployment_tier}"
  task_definition = "${aws_ecs_task_definition.oes_task_def.arn}"

  depends_on = ["aws_alb_target_group.oes-dev-tg"]

  load_balancer {
    container_name = "order-enqueuing-service"
    container_port = 80
    target_group_arn = "${data.terraform_remote_state.info.outputs.ops-dev-tg}"
  }
}

resource "aws_alb_listener_rule" "oes_service" {
  listener_arn = "${data.terraform_remote_state.info.outputs.oes-listener}"
  priority     = "100"

  action {
    type             = "forward"
    target_group_arn = "${data.terraform_remote_state.info.outputs.oes-dev-tg}"
  }

  condition {
    field  = "host-header"
    values = ["oes${var.deployment_tier == "prod" ? "" : "-${var.deployment_tier}"}.phizzard.app"]
  }
}
//data "template_file" "user_data_oes" {
//  template = "${file("${path.module}/../user-data-amazon-linux-dev.sh")}"
//
//  vars = {
//    app_name = "OrderEnqueuingService"
//    aws_key = "${var.aws_access_key_id}"
//    aws_region = "${var.aws_region}"
//    aws_secret = "${var.aws_secret_access_key}"
//    cluster_name = "${var.cluster_name}"
//    container_port = "9080"
//    deployment_tier = "${var.deployment_tier}"
//    inbound_port = "${var.inbound_port}"
//    nr_account_id = "${var.nr_account_id}"
//    nr_insights_key = "${var.nr_insights_key}"
//    nr_license_key = "${var.nr_license_key}"
//    project = "order-enqueuing-service"
//    service = "${var.service}"
//    version = "${var.version-tag}"
//  }
//}
//
//resource "aws_instance" "oes" {
//  ami = "${data.aws_ami.amazon-linux.id}"
//  associate_public_ip_address = true
//  instance_type = "t2.small"
//  key_name = "akr-key-pair1"
//  monitoring = true
//  user_data = "${data.template_file.user_data_oes.rendered}"
//
//  # order svcs security group: order_services_sg
//  security_groups = [
//    "sg-0675d547eab997ae0",
//  ]
//
//  # vpc phizzard subnet for eu-central-1a
//  subnet_id = "subnet-120e2679"
//
//  tags = {
//    Name = "oes-${var.deployment_tier}"
//  }
//}
//
//resource "aws_route53_record" "oes" {
//  name = "oes${var.deployment_tier == "prod" ? "" : "-${var.deployment_tier}"}.phizzard.app"
//  records = [
//    "${aws_instance.oes.public_ip}"]
//  ttl = 300
//  type = "A"
//  zone_id = "${var.route_53_zone_id}"
//}
//
//resource "aws_alb_target_group_attachment" "oes_dev_tg" {
//  port = 80
//  target_group_arn = "${data.terraform_remote_state.info.outputs.ops-dev-tg}"
//}
//
//output "oes-public-ip" {
//  value = "${aws_instance.oes.public_ip}"
//}
