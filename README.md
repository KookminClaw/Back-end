# 📌 Kookmin Claw- Infrastructure Setup

## 📖 프로젝트 소개
Kookmin claw 프로젝트는 학생들에게 필요한 공지사항을 수집하고 개인화된 피드 형태로 제공하는 서비스입니다.

본 단계에서는 AWS CDK를 활용하여 클라우드 인프라를 코드로 구성하고 배포하는 것을 목표로 합니다.

---

## ⚙️ 기술 스택

- **Backend**: Spring Boot / Java
- **Database**: PostgreSQL 15 + pgvector (on EC2)
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
- EC2 내부 PostgreSQL 15 (pgvector 확장 포함)

---

## 🗄️ DB 스키마

PostgreSQL 15 + pgvector 기반으로 구성되며, 마이그레이션 파일은 `db/migration/`에 위치합니다.

| 테이블 | 설명 |
|---|---|
| `department` | 학과 마스터 |
| `user_profile` | 사용자 프로필 및 개인화 추천 데이터 |
| `notice` | 공지 목록 (크롤링 수집) |
| `notice_detail` | 공지 상세 본문 및 AI 요약 |

- `user_profile.keyword_embedding` : `VECTOR(768)` — 관심 키워드 임베딩 (pgvector)
- DB명: `kookminfeed` / 접속 유저: `kookmin`

---

## 🚀 배포 방식

AWS CDK를 이용하여 인프라를 코드로 정의하고 배포했습니다.

```bash
cd infra
cdk synth
cdk deploy