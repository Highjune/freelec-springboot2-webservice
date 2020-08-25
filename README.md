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

첫 번째 단계로  Travis CI와 S3를 연동한다. 실제 배포는 AWS CodeDeploy라는 서비스를 이용한다. 하지만, S3 연동이 먼저 필요한 이유는 Jar 파일을 전달하기 위해서다. CodeDeploy는 저장 기능이 없기 때문에 Travis CI가 빌드한 결과물을 받아서 CodeDeploy가 가져갈 수 있도록 보관할 수 있는 공간이 필요하다. 보통 이럴 때 AWS S3를 이용한다.

### AWS Key(IAM, identity and Access Management) 발급

일반적으로 AWS 서비스에 외부 서비스가 접근할 수 없다. 그러므로 접근 가능한 권한을 가진 Key 를 생성해서 사용해야 한다. AWS에서는 이러한 인증과 관련된 기능을 제공하는 서비스로 IAM(Identity and Access Management) 이 있다.

IAM 은 AWS에서 제공하는 서비스의 접근 방식과 권한을 관리한다. 이 IAM을 통해 Travis CI가 AWS S3와 CodeDeploy에 접근할 수 있도록 하겠다.

첫 번째로 IAM을 발급 받기 위해서 서비스 > IAM > 왼쪽 사이드바 > 사용자 > 사용자 추가 로 이동한다.

- 사용자 세부 정보 설정 > 사용자 이름 를 입력한다.
- AWS 엑세스 유형 선택 > 엑세스 유형 > 프로그래밍 방식 엑세스 를 체크.
- 사용자 권한 설정 > 기존 정책 직접 연결 을 선택.
- 정책 검색 화면에서 2가지 정책을 추가.
  - s3full 검색&체크
  - CodeDeployFull 검색&체크
- 태크 추가 > 태그 Name 값 지정 는 본인이 인지 가능한 정도의 이름으로 만든다.

최종 생성이 완료되면 엑세스 키와 비밀 엑세스 키가 생성된다. 이것을 Travis CI에 등록해준다.

### Travis CI에 IAM 키 등록
Travis CI의 설정 화면으로 이동해서 Travis CI의 환경 변수에

- AWS_ACCESS_KEY (엑세스 키 ID)
- AWS_ACCESS_SECRET_KEY (비밀 엑세스 키)
이 두가지를 변수로 해서 IAM 사용자에서 발급받은 키 값들을 등록. 여기에 등록된 값들은 이제 .travis.yml에서 $AWS_ACCESS_KEY, $AWS_ACCESS_SECRET_KEY란 이름으로 사용할 수 있다.

이제 이 키를 사용해서 Jar를 관리할 S3 버킷을 생성한다.

### AWS S3 버킷 생성

AWS의 S3(Simple Storage Service) 는 일종의 파일 서버다. 순수하게 파일들을 저장하고 접근 권한을 관리, 검색 등을 지원하는 파일 서버의 역할을 한다. S3는 보통 게시글을 쓸 때 나오는 첨부파일 등록을 구현할 때 많이 이용한다. 파일 서버의 역할을 하기 때문인데, Travis CI에서 생성된 Build 파일을 저장 하도록 구성하겠다. S3에 저장된 Build 파일은 이후 CodeDeploy에서 배포할 파일로 가져가도록 구성할 예정.

- 서비스 > S3 > 버킷 만들기 > 버킷 이름 짓기 이 버킷에 배포할 Zip 파일이 모여있는 장소 임을 의미하도록 짓는 것을 추천.
- 버킷 버전 설정 은 별다른 설정 없이 넘어간다.
- 버킷의 보안과 권한 설정 부분에서는 모든 퍼블릭 액세스를 차단 을 체크한다.

### Travis CI의 빌드내용(Jar)을 S3에 올리기 위해 프로젝트(.travis.yml)에 설정 추가

S3가 생성되었으니 이제 S3로 배포 파일을 전달해 보겠다. .travis.yml 파일에 코드를 추가한다.

```
