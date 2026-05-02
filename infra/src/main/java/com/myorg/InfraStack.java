package com.myorg;

import java.util.List;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;

public class InfraStack extends Stack {
    public InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Vpc vpc = Vpc.Builder.create(this, "KookminFeedVpc")
                .maxAzs(2)
                .natGateways(0)
                .subnetConfiguration(List.of(
                        SubnetConfiguration.builder()
                                .name("Public")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build()
                ))
                .build();

        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "KookminFeedEc2SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        securityGroup.addIngressRule(
                Peer.anyIpv4(),
                Port.tcp(80),
                "Allow HTTP access"
        );

        Instance instance = Instance.Builder.create(this, "KookminFeedEc2")
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.MICRO))
                .machineImage(MachineImage.latestAmazonLinux2023())
                .securityGroup(securityGroup)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .build())
                .build();

        instance.addUserData(
                "dnf update -y",
                "dnf install -y nginx",
                "systemctl enable nginx",
                "systemctl start nginx",

                "dnf install -y mariadb105-server",
                "systemctl enable mariadb",
                "systemctl start mariadb",

                // DB 생성
                "mysql -e \"CREATE DATABASE IF NOT EXISTS kookminfeed;\"",

                // 사용자 생성
                "mysql -e \"CREATE USER IF NOT EXISTS 'kookmin'@'localhost' IDENTIFIED BY 'kookmin1234';\"",

                // 권한
                "mysql -e \"GRANT ALL PRIVILEGES ON kookminfeed.* TO 'kookmin'@'localhost';\"",
                "mysql -e \"FLUSH PRIVILEGES;\""
        );

        CfnOutput.Builder.create(this, "EC2PublicIp")
                .value(instance.getInstancePublicIp())
                .build();

        CfnOutput.Builder.create(this, "EC2Url")
                .value("http://" + instance.getInstancePublicIp())
                .build();
    }
}