# coding: UTF-8

#####
#
# baton relay program v1.0
#
# need following command
#  aws  : amazon EC2 only
#  curl : https://ja.wikipedia.org/wiki/CURL
#
#####

import subprocess
import sys

#
# ENV
#

AWS_REGION = "ap-northeast-1"
INSTANCE_INDEX_TAG_NAME = "index"
ALL_INSTANCE_NUMBER = 2
TARGET_EIP = "52.198.32.156"


#
# COMMAND
#

def cmd_get_instance_id_from_eip(eip):
    return " ".join([
        "aws", "ec2",
        "describe-addresses",
        "--region", AWS_REGION,
        "--query", '"Addresses[?PublicIp==`{eip}`].InstanceId"'.format(eip=eip),
        "--output", "text"
    ])


def cmd_get_own_index_from_instance_id(instance_id):
    return " ".join([
        "aws", "ec2",
        "describe-instances",
        "--region", AWS_REGION,
        "--instance-id", instance_id,
        "--query", '"Reservations[].Instances[].Tags[?Key==`{tag}`].Value[]"'.format(tag=INSTANCE_INDEX_TAG_NAME),
        "--output", "text"
    ])


def cmd_get_instance_id_from_instance_index(instance_index):
    return " ".join([
        "aws", "ec2",
        "describe-instances",
        "--region", AWS_REGION,
        "--filter",
        '"Name=tag-key,Values={tag}"'.format(tag=INSTANCE_INDEX_TAG_NAME),
        '"Name=tag-value,Values={instance_index}"'.format(instance_index=instance_index),
        "--query", '"Reservations[].Instances[].InstanceId[]"',
        "--output", "text"
    ])


def cmd_wakeup_next_instance(instance_id):
    return " ".join([
        "aws", "ec2",
        "start-instances",
        "--region", AWS_REGION,
        "--instance-id", instance_id
    ])


def cmd_check_running_instance(instance_id):
    return " ".join([
        "aws", "ec2",
        "wait", "instance-status-ok",
        "--region", AWS_REGION,
        "--instance-id", instance_id
    ])


def cmd_get_instance_ip_from_instance_id(instance_id):
    return " ".join([
        "aws", "ec2",
        "describe-instances",
        "--region", AWS_REGION,
        "--instance-id", instance_id,
        "--query", '"Reservations[].Instances[].NetworkInterfaces[].Association[].PublicIp"',
        "--output", "text"
    ])


#
# MAIN
#
def main():
    print("##########[start]##########")

    # get instance id from EIP
    instance_id = subprocess.getoutput(cmd_get_instance_id_from_eip(TARGET_EIP))
    print("Instance ID={instance_id}".format(instance_id=instance_id))

    # get instance index from instance id
    instance_index_str = subprocess.getoutput(cmd_get_own_index_from_instance_id(instance_id))
    if instance_index_str == "":
        sys.exit(1)
    instance_index = int(instance_index_str)
    print("Instance index={instance_index}".format(instance_index=instance_index))

    # get next instance index
    next_instance_index = (instance_index + 1) % ALL_INSTANCE_NUMBER
    print("Next instance index={next_instance_index}".format(next_instance_index=next_instance_index))

    # get next instance id from instance index
    next_instance_id = subprocess.getoutput(cmd_get_instance_id_from_instance_index(next_instance_index))
    print("Next instance ID={next_instance_id}".format(next_instance_id=next_instance_id))

    # wake up next instance
    print("Wakeup next instance")
    subprocess.getoutput(cmd_wakeup_next_instance(next_instance_id))
    subprocess.getoutput(cmd_check_running_instance(next_instance_id))
    print("Next instance is ready!")

    # Get wakeup instance IP
    next_instance_ip = subprocess.getoutput(cmd_get_instance_ip_from_instance_id(next_instance_id))
    print("Next instance IP={next_instance_ip}".format(next_instance_ip=next_instance_ip))

    # TODO: IPに対してSSHでdocker-compose down & up -dする

    # TODO: EIPを付け替える

    # TODO: 古いインスタンスをシャットダウンする

    print("##########[end  ]##########")


# RUN
if __name__ == '__main__':
    main()
