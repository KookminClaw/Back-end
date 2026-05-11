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

                // DB 및 사용자 생성
                "mysql -e \"CREATE DATABASE IF NOT EXISTS kookminfeed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\"",
                "mysql -e \"CREATE USER IF NOT EXISTS 'kookmin'@'localhost' IDENTIFIED BY 'kookmin1234';\"",
                "mysql -e \"GRANT ALL PRIVILEGES ON kookminfeed.* TO 'kookmin'@'localhost';\"",
                "mysql -e \"FLUSH PRIVILEGES;\"",

                // 스키마 초기화 (V1__init_schema.sql 인라인 실행)
                "mysql kookminfeed << 'SQL_EOF'",
                "CREATE TABLE IF NOT EXISTS department (",
                "    code VARCHAR(20)  NOT NULL,",
                "    name VARCHAR(100) NOT NULL,",
                "    PRIMARY KEY (code)",
                ");",

                "CREATE TABLE IF NOT EXISTS user_profile (",
                "    user_id                   CHAR(36)    NOT NULL,",
                "    student_number            VARCHAR(20) NOT NULL,",
                "    created_at                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,",
                "    updated_at                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,",
                "    profile_completion_rate   SMALLINT    NULL,",
                "    grade                     SMALLINT    NOT NULL,",
                "    department_code           VARCHAR(20) NOT NULL,",
                "    enrollment_status         VARCHAR(20) NOT NULL DEFAULT 'enrolled',",
                "    interest_keywords         JSON        NULL,",
                "    career_goals              JSON        NULL,",
                "    course_interests          JSON        NULL,",
                "    extracurricular_interests JSON        NULL,",
                "    scholarship_interest      BOOLEAN     NULL DEFAULT TRUE,",
                "    notify_push               BOOLEAN     NOT NULL DEFAULT TRUE,",
                "    notify_email              BOOLEAN     NOT NULL DEFAULT FALSE,",
                "    notify_categories         JSON        NULL,",
                "    last_active_at            DATETIME    NULL,",
                "    PRIMARY KEY (user_id),",
                "    UNIQUE KEY uq_student_number (student_number),",
                "    CONSTRAINT fk_up_department FOREIGN KEY (department_code) REFERENCES department (code),",
                "    CONSTRAINT chk_grade CHECK (grade BETWEEN 1 AND 5),",
                "    CONSTRAINT chk_profile_completion CHECK (profile_completion_rate IS NULL OR profile_completion_rate BETWEEN 0 AND 100),",
                "    CONSTRAINT chk_enrollment_status CHECK (enrollment_status IN ('enrolled', 'leave', 'graduated'))",
                ");",

                "CREATE TABLE IF NOT EXISTS notice (",
                "    id           BIGINT       NOT NULL AUTO_INCREMENT,",
                "    title        VARCHAR(500) NOT NULL,",
                "    link         VARCHAR(1000) NOT NULL,",
                "    published    DATETIME     NOT NULL,",
                "    source       VARCHAR(200) NOT NULL,",
                "    category     ENUM('학사','장학','비교과','취업','행사','기타') DEFAULT '기타',",
                "    importance   TINYINT      DEFAULT 0,",
                "    deadline     DATETIME,",
                "    target_grade VARCHAR(20),",
                "    PRIMARY KEY (id),",
                "    UNIQUE KEY uq_link (link(255))",
                ");",

                "CREATE TABLE IF NOT EXISTS notice_detail (",
                "    id        BIGINT   NOT NULL AUTO_INCREMENT,",
                "    notice_id BIGINT   NOT NULL,",
                "    body      LONGTEXT,",
                "    attachments TEXT,",
                "    summary   TEXT,",
                "    PRIMARY KEY (id),",
                "    UNIQUE KEY uq_notice (notice_id),",
                "    CONSTRAINT fk_nd_notice FOREIGN KEY (notice_id) REFERENCES notice (id)",
                ");",
                "SQL_EOF"
        );

        CfnOutput.Builder.create(this, "EC2PublicIp")
                .value(instance.getInstancePublicIp())
                .build();

        CfnOutput.Builder.create(this, "EC2Url")
                .value("http://" + instance.getInstancePublicIp())
                .build();
    }
}