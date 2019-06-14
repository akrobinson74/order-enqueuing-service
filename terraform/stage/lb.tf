terraform {
  backend "s3" {
    bucket = "terraform-phizzard-kotlin"
    key    = "aws/lb/terraform.tfstate"
    region = "eu-central-1"
  }
}

resource "aws_alb" "service" {
  name = "alb-order-services"
  internal = false
  security_groups = ["${aws_security_group.ecs-elb.id}"]
  subnets = ["${aws_subnet.public.*.id}"]
//  vpc_id = "${aws_vpc.order_services.id}"

  depends_on = ["data.aws_subnet_ids.vpc","data.terraform_remote_state.ecs"]
}

resource "aws_lb_target_group" "test" {
  health_check {
    interval = "30"
    matcher = "200"
    path = "/health"
    port = "traffic-port"
    protocol = "HTTP"
  }
  name = "default-tg"
  port = 9080
  protocol = "HTTP"
  stickiness {
    type = "lb_cookie"
  }
  vpc_id = "${aws_vpc.order_services.id}"
}

resource "aws_alb_listener" "service" {
  load_balancer_arn = "${aws_alb.service.arn}"
  port = 9080
  protocol = "HTTP"
  depends_on = ["aws_alb.service", "aws_lb_target_group.test"]

  default_action {
    target_group_arn = "$"
    type = "forward"
  }
}

output "arn" {
  value = "${aws_alb.service.arn}"
}