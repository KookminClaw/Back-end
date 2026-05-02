# 📌 Kookmin Claw- Infrastructure Setup

## 📖 프로젝트 소개
Kookmin Feed 프로젝트는 학생들에게 필요한 공지사항을 수집하고 개인화된 피드 형태로 제공하는 서비스입니다.

본 단계에서는 AWS CDK를 활용하여 클라우드 인프라를 코드로 구성하고 배포하는 것을 목표로 합니다.

---

## ⚙️ 기술 스택

- **Backend**: Spring Boot / Java
- **Database**: MySQL (MariaDB on EC2)
- **Cloud**: AWS
- **Infrastructure as Code**: AWS CDK (Java)
- **Server**: Amazon EC2 (t3.micro)

---

## 🏗️ 인프라 구성

AWS CDK(Java)를 사용하여 다음 리소스를 생성했습니다.

### 📌 생성된 리소스

- VPC
- Public Subnet
- Security Group
- EC2 Instance
- nginx 웹 서버
- EC2 내부 MariaDB (MySQL 호환 DB)

---

## 🚀 배포 방식

AWS CDK를 이용하여 인프라를 코드로 정의하고 배포했습니다.

```bash
cd infra
cdk synth
cdk deploy