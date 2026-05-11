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

                // PostgreSQL 15 설치
                "dnf install -y postgresql15-server postgresql15",
                "postgresql-setup --initdb",
                "systemctl enable postgresql",
                "systemctl start postgresql",

                // pgvector 빌드 및 설치
                "dnf install -y gcc make postgresql15-devel git",
                "cd /tmp && git clone https://github.com/pgvector/pgvector.git",
                "cd /tmp/pgvector && make && make install",

                // DB 및 사용자 생성
                "sudo -u postgres psql -c \"CREATE DATABASE kookminfeed;\"",
                "sudo -u postgres psql -c \"CREATE USER kookmin WITH PASSWORD 'kookmin1234';\"",
                "sudo -u postgres psql -c \"GRANT ALL PRIVILEGES ON DATABASE kookminfeed TO kookmin;\"",
                "sudo -u postgres psql -d kookminfeed -c \"GRANT ALL ON SCHEMA public TO kookmin;\"",

                // 스키마 초기화
                "sudo -u postgres psql -d kookminfeed << 'SQL_EOF'",
                "CREATE EXTENSION IF NOT EXISTS vector;",

                "CREATE TABLE IF NOT EXISTS department (",
                "    code VARCHAR(20)  NOT NULL,",
                "    name VARCHAR(100) NOT NULL,",
                "    PRIMARY KEY (code)",
                ");",

                "CREATE TABLE IF NOT EXISTS user_profile (",
                "    user_id                   UUID        NOT NULL DEFAULT gen_random_uuid(),",
                "    student_number            VARCHAR(20) NOT NULL,",
                "    created_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),",
                "    updated_at                TIMESTAMPTZ NOT NULL DEFAULT NOW(),",
                "    profile_completion_rate   SMALLINT    NULL,",
                "    grade                     SMALLINT    NOT NULL,",
                "    department_code           VARCHAR(20) NOT NULL,",
                "    enrollment_status         VARCHAR(20) NOT NULL DEFAULT 'enrolled',",
                "    interest_keywords         TEXT[]      NULL,",
                "    career_goals              TEXT[]      NULL,",
                "    course_interests          TEXT[]      NULL,",
                "    extracurricular_interests TEXT[]      NULL,",
                "    scholarship_interest      BOOLEAN     NULL DEFAULT TRUE,",
                "    notify_push               BOOLEAN     NOT NULL DEFAULT TRUE,",
                "    notify_email              BOOLEAN     NOT NULL DEFAULT FALSE,",
                "    notify_categories         TEXT[]      NULL,",
                "    keyword_embedding         VECTOR(768) NULL,",
                "    last_active_at            TIMESTAMPTZ NULL,",
                "    PRIMARY KEY (user_id),",
                "    UNIQUE (student_number),",
                "    CONSTRAINT fk_up_department FOREIGN KEY (department_code) REFERENCES department (code),",
                "    CONSTRAINT chk_grade CHECK (grade BETWEEN 1 AND 5),",
                "    CONSTRAINT chk_profile_completion CHECK (profile_completion_rate IS NULL OR profile_completion_rate BETWEEN 0 AND 100),",
                "    CONSTRAINT chk_enrollment_status CHECK (enrollment_status IN ('enrolled', 'leave', 'graduated'))",
                ");",

                "CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;",
                "CREATE TRIGGER trg_user_profile_updated_at BEFORE UPDATE ON user_profile FOR EACH ROW EXECUTE FUNCTION set_updated_at();",
                "CREATE INDEX IF NOT EXISTS idx_up_keyword_embedding ON user_profile USING ivfflat (keyword_embedding vector_cosine_ops);",

                "CREATE TYPE notice_category AS ENUM ('학사','장학','비교과','취업','행사','기타');",

                "CREATE TABLE IF NOT EXISTS notice (",
                "    id           BIGSERIAL       NOT NULL,",
                "    title        VARCHAR(500)    NOT NULL,",
                "    link         VARCHAR(1000)   NOT NULL,",
                "    published    TIMESTAMPTZ     NOT NULL,",
                "    source       VARCHAR(200)    NOT NULL,",
                "    category     notice_category DEFAULT '기타',",
                "    importance   SMALLINT        DEFAULT 0,",
                "    deadline     TIMESTAMPTZ,",
                "    target_grade VARCHAR(20),",
                "    PRIMARY KEY (id),",
                "    UNIQUE (link)",
                ");",

                "CREATE TABLE IF NOT EXISTS notice_detail (",
                "    id        BIGSERIAL NOT NULL,",
                "    notice_id BIGINT    NOT NULL,",
                "    body      TEXT,",
                "    attachments TEXT,",
                "    summary   TEXT,",
                "    PRIMARY KEY (id),",
                "    UNIQUE (notice_id),",
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