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
  default = "order-enqueuing-service-cluster"
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
  name = "${var.cluster_name}"
}

resource "aws_iam_role" "oes_iam_instance_role" {
  name = "oes-iam-instance-role"

  assume_role_policy = <<EOF
{
 "Version": "2008-10-17",
 "Statement": [
   {
     "Sid": "",
     "Effect": "Allow",
     "Principal": {
       "Service": "ec2.amazonaws.com"
     },
     "Action": "sts:AssumeRole"
   }
 ]
}
EOF
}

resource "aws_iam_role_policy" "oes_iam_instance_role_policy" {
  name = "oes-iam-instance-role-policy"
  role = "${aws_iam_role.oes_iam_instance_role.id}"

  policy = <<EOF
{
 "Version": "2012-10-17",
 "Statement": [
   {
     "Effect": "Allow",
     "Action": [
       "ecs:CreateCluster",
       "ecs:DeregisterContainerInstance",
       "ecs:DiscoverPollEndpoint",
       "ecs:Poll",
       "ecs:RegisterContainerInstance",
       "ecs:StartTelemetrySession",
       "ecs:Submit*",
       "ecr:GetAuthorizationToken",
       "ecr:BatchCheckLayerAvailability",
       "ecr:GetDownloadUrlForLayer",
       "ecr:BatchGetImage",
       "logs:CreateLogStream",
       "logs:PutLogEvents"
     ],
     "Resource": "*"
   }
 ]
}
EOF
}

resource "aws_iam_role" "oes_iam_service_role" {
  name = "oes-iam-service-role"

  assume_role_policy = <<EOF
{
 "Version": "2008-10-17",
 "Statement": [
   {
     "Sid": "",
     "Effect": "Allow",
     "Principal": {
       "Service": "ecs.amazonaws.com"
     },
     "Action": "sts:AssumeRole"
   }
 ]
}
EOF
}

resource "aws_iam_role_policy" "oes_iam_service_role_policy" {
  name = "oes-iam-service-role-policy"
  role = "${aws_iam_role.oes_iam_service_role.id}"

  policy = <<EOF
{
 "Version": "2012-10-17",
 "Statement": [
   {
     "Effect": "Allow",
     "Action": [
       "ec2:AuthorizeSecurityGroupIngress",
       "ec2:Describe*",
       "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
       "elasticloadbalancing:DeregisterTargets",
       "elasticloadbalancing:Describe*",
       "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
       "elasticloadbalancing:RegisterTargets"
     ],
     "Resource": "*"
   }
 ]
}
EOF
}

resource "aws_iam_instance_profile" "oes_instance_profile" {
  name = "oes-iam-instance-profile"
  role = "${aws_iam_role.oes_iam_instance_role.name}"
}

data "template_file" "user_data" {
  template = "${file("${path.module}/user-data.sh")}"

  vars = {
    cluster_name = "${var.cluster_name}"
  }
}

resource "aws_launch_configuration" "oes_dev_launch" {
  iam_instance_profile = "${aws_iam_instance_profile.oes_instance_profile.id}"
  image_id = "${data.aws_ami.amazon-linux.id}"
  instance_type = "t2.small"
  key_name = "akr-key-pair1"
  name_prefix = "oes-launch-conf-"
  security_groups = ["${data.terraform_remote_state.info.outputs.order_services_sg}"]

  root_block_device {
    volume_size = "8"
  }

  user_data = "${data.template_file.user_data.rendered}"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "oes_scaling" {
  availability_zones = [
    "eu-central-1a",
    "eu-central-1b",
    "eu-central-1c"]
  default_cooldown = "300"
  depends_on = ["aws_launch_configuration.oes_dev_launch"]
  desired_capacity = "2"
  health_check_grace_period = "300"
  health_check_type = "EC2"
  launch_configuration = "${aws_launch_configuration.oes_dev_launch.name}"
  max_size = "6"
  min_size = "2"
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
    container_port = "9080"
    containerPort = 9080
    deployment_tier = "${var.deployment_tier}"
    env = "dev"
    host_name = "oes${var.deployment_tier == "prod" ? "" : "-${var.deployment_tier}"}.phizzard.app"
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
  network_mode = "bridge"
}

resource "aws_alb_target_group" "oes-ecs-tg" {
  name = "oes-ecs-tg"
  port = 80
  protocol = "HTTP"
  vpc_id = "${data.terraform_remote_state.info.outputs.vpc_phizzard.id}"

  health_check {
    path = "/health"
    matcher = "200"
  }

  stickiness {
    type = "lb_cookie"
  }
}

data "aws_acm_certificate" "sslcert" {
  domain = "pro.oes.phizzard.app"
}

resource "aws_alb" "oes_alb" {
  name = "oes-alb"
  internal = false
  security_groups = ["${data.terraform_remote_state.info.outputs.order_services_sg}"]
  subnets = [
    "${data.terraform_remote_state.info.outputs.subnet-euc-1a}",
    "${data.terraform_remote_state.info.outputs.subnet-euc-1b}",
    "${data.terraform_remote_state.info.outputs.subnet-euc-1c}"
  ]
}

resource "aws_alb_listener" "oes_dev_listener" {
  default_action {
    type = "forward"
    target_group_arn = "${aws_alb_target_group.oes-ecs-tg.arn}"
  }

  certificate_arn = "${data.aws_acm_certificate.sslcert.arn}"
  load_balancer_arn = "${aws_alb.oes_alb.arn}"
  port = 443
  protocol = "HTTPS"
  ssl_policy = "ELBSecurityPolicy-2016-08"
}

resource "aws_ecs_service" "oes_service" {
  cluster = "${aws_ecs_cluster.oes_cluster.id}"
  deployment_maximum_percent = 200
  deployment_minimum_healthy_percent = 50
  desired_count = 2
  iam_role = "${aws_iam_role.oes_iam_service_role.arn}"
  name = "oes-ecs-${var.deployment_tier}"
  task_definition = "${aws_ecs_task_definition.oes_task_def.arn}"

  depends_on = ["aws_alb_target_group.oes-ecs-tg","aws_iam_role.oes_iam_service_role"]

  ordered_placement_strategy {
    type = "spread"
    field = "instanceId"
  }

  load_balancer {
    container_name = "order-enqueuing-service"
    container_port = 9080
    target_group_arn = "${aws_alb_target_group.oes-ecs-tg.id}"
  }
}

resource "aws_alb_listener_rule" "oes_service" {
  listener_arn = "${aws_alb_listener.oes_dev_listener.arn}"
  priority     = "100"

  action {
    type             = "forward"
    target_group_arn = "${aws_alb_target_group.oes-ecs-tg.arn}"
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
//  target_id = ""
//}
//
//output "oes-public-ip" {
//  value = "${aws_instance.oes.public_ip}"
//}
