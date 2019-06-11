provider "aws" {
  region = "eu-central-1"
}

data "aws_ami" "ubuntu" {
  most_recent = true

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-bionic-*"]
  }

  owners = ["099720109477"]
}

resource "aws_instance" "oes" {
  ami                         = "${data.aws_ami.ubuntu.id}"
  associate_public_ip_address = true
  instance_type               = "t2.micro"
  key_name                    = "akr-key-pair1"
  monitoring                  = true

  security_groups = [
    "sg-0b2ac6867f9311863",
  ]

  subnet_id = "subnet-a904c8d2"

  tags = {
    Name = "oes-staging"
  }
}

resource "aws_instance" "ops" {
  ami                         = "${data.aws_ami.ubuntu.id}"
  associate_public_ip_address = true
  instance_type               = "t2.micro"
  key_name                    = "akr-key-pair1"
  monitoring                  = true

  security_groups = [
    "sg-0b2ac6867f9311863",
  ]

  subnet_id = "subnet-a904c8d2"

  tags = {
    Name = "ops-staging"
  }
}

output "ops-public-ip" {
  value = "${aws_instance.ops.public_ip}"
}
