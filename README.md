# freelec-springboot2-webservice


### CI & CD
코드 버전 관리를 하는 VCS 시스템(git, svn 등)에 push를 하면 자동으로 테스트와 빌드가 수행되어 안정적인 배포 파일을 만드는 과정을 CI(Continuous Integration- 지속적 통합) 이라고 하며, 이 빌드 경과를 자동으로 운영 서버에 무중단 배포까지 진행되는 과정을 CD(Continuous Deployment-지속적인 배포) 라고 한다

### Github과 Travis CI 연동
Travis CI는 깃허브에서 제공하는 무료 CI 서비스이다. 젠킨스와 같은 CI 도구도 있으나, 젠킨스는 설치형이므로 이를 위한 EC2 인스턴스가 하나 더 필요하다.

Travis CI에 접속해서 깃허브 계정으로 로그인 한 뒤, 좌측 위에 계정명 > Setting을 클릭하여 해당 프로젝트를 활성화 시킨다.

### 프로젝트에 Travis CI(.travis.yml) 설정
Travis CI의 상세한 설정은 프로젝트에 존재하는 .travis.yml 파일로 할 수 있다. build.gradle과 같은 위치에 해당 파일 생성

```
language: java
jdk:
  - openjdk8

branches: # (1)
  only:
    - master

before_install: # 권한 주는 것
  - chmod +x gradlew

# Travis CI 서버의 Home
cache: # (2)
  directories:
    - '$HOME/.m2/repository'
    - '$HOME/.gradle'

script: "./gradlew clean build" # (3)

# CI 실행 완료 시 메일로 알람
notifications: # (4)
  email:
    recipients:
      - 본인 이메일
```
(1) branches
- Travis CI를 어느 브랜치가 푸시될 때 수행할지 지정
- 현재 옵션은 오직 master브랜치에 push 될 때만 수행

(2) cache
- 그레이들을 통해 의존성을 받게 되면 이를 해당 디렉토리에 캐시하여, 같은 의존성은 다음 배포때부터 다시 받지 않도록 설정

(3) script
- master 브랜치에 푸시되었을 때 수행하는 명령어
- 여기서는 프로젝트 내부에 둔 gradlew을 통해 clean & build 를 수행

(4) notification
- Travis CI 실행 완료 시 자동으로 알람이 가도록 설정

### Travis CI와 AWS S3 연동
S3란 AWS에서 제공하는 일종의 파일 서버이다. 이미지 파일을 비롯한 정적 파일들을 관리하거나 지금 진행하는 것처럼 배포 파일들을 관리하는 등의 기능을 지원한다. 보통 이미지 업로드를 구혀난다면 이 S3를 이용하여 구현하는 경우가 많다. S3를 비롯한 AWS 서비스와 Travis CI를 연동하게 되면 전체 구조는 다음과 같다. 

![process](https://user-images.githubusercontent.com/57219160/90869301-fcac2980-e3d2-11ea-98f9-f587f1726902.png)

첫 번째 단계로 
