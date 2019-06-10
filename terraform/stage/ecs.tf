terraform {
  backend "s3" {
    bucket = "terraform-phizzard-kotlin"
    key = "aws/ecs/terraform.tfstate"
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

data "aws_ami" "ecs_ami" {
  most_recent = true
  owners = [
    "amazon"]

  filter {
    name = "name"
    values = [
      "amzn-ami-*-amazon-ecs-optimized"]
  }
}

resource "aws_ecs_cluster" "ecs" {
  name = "order-services"
}

resource "aws_cloudwatch_log_group" "services" {
  name = "orders-services-cloudwatch-log-group"
  depends_on = ["aws_ecs_cluster.ecs"]
}

resource "aws_iam_role" "cluster" {
  name = "orders-clusterrole"
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

resource "aws_iam_role" "services" {
  name = "orders-servicesrole"
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

resource "aws_iam_role_policy" "cluster" {
  name = "orders-cluster-iam-role-policy"
  role = "${aws_iam_role.cluster.id}"

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
        "ec2:AuthorizeSecurityGroupEgress",
        "ec2:AuthorizeSecurityGroupIngress",
        "ec2:CreateSecurityGroup",
        "ec2:DeleteSecurityGroup",
        "s3:*",
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "services" {
  name = "orders-services-iam-role-policy"
  role = "${aws_iam_role.services.id}"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "elasticloadbalancing:Describe*",
        "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
        "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
        "elasticloadbalancing:DeregisterTargets",
        "elasticloadbalancing:RegisterTargets",
        "ec2:Describe*",
        "ec2:AuthorizeSecurityGroupEgress",
        "ec2:AuthorizeSecurityGroupIngress",
        "ec2:CreateSecurityGroup",
        "ec2:DeleteSecurityGroup",
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
EOF
}

resource "random_id" "code" {
  byte_length = 4
}

resource "aws_iam_instance_profile" "cluster" {
  name = "orders-instance-profile-${random_id.code.hex}"
  role = "${aws_iam_role.cluster.name}"
}

resource "aws_security_group" "ecs_instance" {
  name = "ecs-instance"
  description = "Instance SG for ECS"
  vpc_id = "${aws_vpc.order_services.id}"
}

resource "aws_security_group_rule" "inbound_all" {
  cidr_blocks = ["0.0.0.0/0"]
  from_port = 0
  protocol = "-1"
  security_group_id = "${aws_security_group.ecs_instance.id}"
  to_port = 0
  type = "ingress"
}

resource "aws_security_group_rule" "outbound_all" {
  cidr_blocks = ["0.0.0.0/0"]
  from_port = 0
  protocol = "-1"
  security_group_id = "${aws_security_group.ecs_instance.id}"
  to_port = 0
  type = "egress"
}

resource "aws_security_group" "ecs-elb" {
  description = "elb for ecs"
  vpc_id = "${aws_vpc.order_services.id}"

  egress {
    from_port = 0
    protocol = -1
    to_port = 0
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  ingress {
    from_port = 9080
    protocol = "tcp"
    to_port = 9080
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  ingress {
    from_port = 9081
    protocol = "tcp"
    to_port = 9081
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags {
    Name = "ELB Security Group"
  }
}

data "aws_subnet_ids" "vpc" {
  vpc_id = "${aws_vpc.order_services.id}"

}

data "template_file" "user_data" {
  template = "${file("${path.module}/user-data.sh")}"

  vars {
    aws_region = "${var.aws_region}"
    ecs_cluster_name = "${aws_ecs_cluster.ecs.name}"
  }
}

resource aws_launch_configuration "orders" {
  name = "order-services-launch-config"
  enable_monitoring = true
  image_id = "${data.aws_ami.ecs_ami.name}"
  instance_type = "t2.micro"
  key_name = "akr-key-pair1"
  security_groups = [
    "${aws_security_group.ecs-elb.id}"]

  root_block_device {
    volume_size = "8"
  }

  user_data = "${data.template_file.user_data.rendered}"
}

//resource aws_autoscaling_group "oes" {
//  name = "orders-oes-autoscaling-group"
//  availability_zones = ["${var.aws_availability_zones}"]
//  desired_capacity = "2"
//  launch_configuration = ""
//  max_size = "4"
//  min_size = "1"
//
//
//}
//
//resource aws_autoscaling_group "oes" {
//  name = "orders-oes-autoscaling-group"
//  availability_zones = ["${var.aws_availability_zones}"]
//  desired_capacity = "2"
//  launch_configuration = ""
//  max_size = "4"
//  min_size = "1"
//
//}

output "ecs-cluster-id" {
  value = "${aws_ecs_cluster.ecs.id}"
}

output "ecs-elb-securitygroup-id" {
  value = "${aws_security_group.ecs-elb.id}"
}

output "ecs-instance-securitygroup-id" {
  value = "${aws_security_group.ecs_instance.id}"
}

output "ecs-servicerole-arn" {
  value = "${aws_iam_role.services.arn}"
}

output "elb_security_group_id" {
  value = "${aws_security_group.ecs-elb.id}"
}

output "public_subnet_ids" {
  value = [
    "${aws_subnet.public.*.id}"]
}