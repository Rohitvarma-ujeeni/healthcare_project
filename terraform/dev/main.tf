provider "aws" {
  region = "ap-south-1"
}

# Get existing security group by name
data "aws_security_group" "existing_sg" {
  name = "launch-wizard-1"
}

# Create EC2 instance
resource "aws_instance" "jenkins_test" {
  ami                    = "ami-021a584b49225376d"
  instance_type          = "t2.micro"
  key_name               = "Slave1"
  vpc_security_group_ids = [data.aws_security_group.existing_sg.id]

  tags = {
    Name = "dev-app"
  }
}

# Output EC2 instance private IP
output "instance_ip" {
  description = "Private IP of the EC2 instance"
  value       = aws_instance.jenkins_test.private_ip
}