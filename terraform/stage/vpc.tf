terraform {
  backend "s3" {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/vpc/terraform.tfstate"
    region = "eu-central-1"
  }
}

resource "aws_vpc" "order_services" {
  cidr_block = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support = true
  tags {
    Name = "order services vpc"
  }
}

resource "aws_internet_gateway" "order_services_gtw" {
  depends_on = [
    "aws_vpc.order_services"]
  vpc_id = "${aws_vpc.order_services.id}"

  tags {
    Name = "order services public gateway"
  }
}

resource "aws_subnet" "public" {
  count = "${length(var.aws_availability_zones)}"
  vpc_id = "${aws_vpc.order_services.id}"
  availability_zone = "${element(var.aws_availability_zones, count.index)}"
  cidr_block = "10.0.${(count.index+1) * 10}.0/24"
  map_public_ip_on_launch = true

  depends_on = [
    "aws_internet_gateway.order_services_gtw"]

  tags {
    Name = "public-${element(var.aws_availability_zones, count.index)}"
  }
}

resource "aws_route_table" "public" {
  vpc_id = "${aws_vpc.order_services.id}"

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.order_services_gtw.id}"
  }
}

resource "aws_route" "public" {
  count = "${length(var.aws_availability_zones)}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = "${aws_internet_gateway.order_services_gtw.id}"
  route_table_id = "${aws_route_table.public.id}"
  subnet_id = "${element(aws_subnet.public.*.id, count.index)}"
}

resource "aws_route_table_association" "public" {
  count = "${length(var.aws_availability_zones)}"
  route_table_id = "${aws_route_table.public.id}"
  subnet_id = "${element(aws_subnet.public.*.id, count.index)}"
}

output "aws_region" {
  value = "${var.aws_region}"
}

output "vpc_id" {
  value = "${aws_vpc.order_services.id}"
}