output "phizzard-service-role" {
  value = "${data.aws_iam_role.phizzard_service.arn}"
}

output "vpc_phizzard" {
  value = "${data.aws_vpc.vpc_phizzard}"
}

output "oes-orderservices-elb" {
  value = "${data.aws_alb.orderservices-elb}"
}

output "oes-listener" {
  value = "${data.aws_alb_listener.oes-listener.arn}"
}

output "ops-orderservices-elb" {
  value = "${data.aws_alb.ops-orderservices-elb}"
}

output "oes-dev-tg" {
  value = "${data.aws_alb_target_group.oes-dev-tg.arn}"
}

output "ops-dev-tg" {
  value = "${data.aws_alb_target_group.ops-dev-tg.arn}"
}

output "ops-stage-tg" {
  value = "${data.aws_alb_target_group.ops-stage-tg.arn}"
}

output "ops-prod-tg" {
  value = "${data.aws_alb_target_group.ops-tg.arn}"
}

output "phizzard-route-53" {
  value = "${data.aws_route53_zone.phizzard-route-53.id}"
}

output "order_services_sg" {
  value = "${data.aws_security_group.order_services_sg.id}"
}

output "subnet-euc-1a" {
  value = "${data.aws_subnet.euc-1a.id}"
}

output "subnet-euc-1b" {
  value = "${data.aws_subnet.euc-1b.id}"
}

output "subnet-euc-1c" {
  value = "${data.aws_subnet.euc-1c.id}"
}