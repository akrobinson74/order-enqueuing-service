terraform {
  backend "s3" {
    bucket = "kotlin-terraform"
    key = "dev/orderservices/oes/terraform.tfstate"
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

resource "aws_alb_target_group_attachment" "oes_dev_tg" {
  port = 80
  target_group_arn = "arn:aws:elasticloadbalancing:eu-central-1:806353235757:targetgroup/oes-dev-tg/dd82a028c9f90137"
  target_id = "${aws_instance.oes.id}"
}

output "oes-public-ip" {
  value = "${aws_instance.oes.public_ip}"
}
/*
resource "aws_route53_record" "dev_oes" {

  name = "${var.deployment_tier == "prod" ? "" : "${var.deployment_tier}"}.oes.phizzard.app"
  alias {
    evaluate_target_health = true
    # both name and zone_id taken from route53 "Hosted zones" dashboard
    name = "dualstack.orderservices-elb-1322745023.eu-central-1.elb.amazonaws.com."
    zone_id = "Z215JYRZR1TBD5"
  }
  type = "A"
  zone_id = "${var.route_53_zone_id}"
}

resource "aws_s3_bucket" "oes_dev_site_bucket" {
  bucket = "${aws_route53_record.oes.name}"
  acl = "public-read"

  website {
    index_document = "index.html"
    error_document = "error.html"
  }
}

# cloudfront distribution
resource "aws_cloudfront_distribution" "site_distribution" {
  origin {
    domain_name = "${aws_s3_bucket.oes_dev_site_bucket.bucket_domain_name}"
    origin_id = "${aws_route53_record.oes.name}-origin"
  }
  enabled = true
  aliases = ["${aws_route53_record.oes.name}"]
  price_class = "PriceClass_100"
  default_root_object = "index.html"
  default_cache_behavior {
    allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH",
      "POST", "PUT"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "${aws_route53_record.oes.name}-origin"
    forwarded_values {
      query_string = true
      cookies {
        forward = "all"
      }
    }
    viewer_protocol_policy = "https-only"
    min_ttl                = 0
    default_ttl            = 1000
    max_ttl                = 86400
  }
  restrictions {
    geo_restriction {
      restriction_type = "whitelist"
      locations = ["DE"]
    }
  }
  viewer_certificate {
    acm_certificate_arn = "arn:aws:acm:eu-central-1:806353235757:certificate/eacb8bc0-13ce-4707-b565-1cbfc4e7496e"
    ssl_support_method  = "sni-only"
    minimum_protocol_version = "TLSv1.1_2016" # defaults wrong, set
  }
}
# arn:aws:acm:eu-central-1:806353235757:certificate/eacb8bc0-13ce-4707-b565-1cbfc4e7496e
*/