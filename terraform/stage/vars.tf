variable "aws_availability_zones" {
  default = [
    "eu-central-1a",
    "eu-central-1b",
    "eu-central-1c"
  ]
}
variable "aws_region" {
  default = "eu-central-1"
}

variable "aws_access_key_id" {}
variable "aws_secret_access_key" {}