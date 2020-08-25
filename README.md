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

- .travis.yml

```
script: "./gradlew clean build"

before_deploy: # (1)
  - zip -r freelec-springboot2-webservice * # (2)
  - mkdir -p deploy # (3)
  - mv freelec-springboot2-webservice.zip deploy/freelec-springboot2-webservice.zip # (4)

deploy: # (5)
  - provider: s3
    access_key_id: $AWS_ACCESS_KEY # Travis repo setting에 설정된 값
    secret_access_key: $AWS_SECRET_KEY # Travis repo setting에 설정된 값
    bucket: freelec-springboot-doop-build # S3 버킷
    region: ap-northeast-2
    skip_cleanup: true
    acl: private # zip 파일 접근을 private으로
    local_dir: deploy # before_deploy에서 생성한 디렉토리 # (6)
    wait-until-deployed: true
```

(1) ```before_deploy```
- deploy 명령어가 실행되기 전에 수행된다
- CodeDeploy는 Jar 파일은 인식하지 못하므로 Jar + 기타 설정 파일들을 모아 압축(zip)한다.

(2) ```zip -r freelec-springboot2-webservice```
- 현재 위치의 모든 파일을 freelec-springboot2-webservice 이름으로 압축(zip)한다.
- 명령어의 마지막 위치는 본인의 프로젝트 이름이어야 한다.

(3) ```mkdir -p deploy```
- deploy라는 디렉토리를 Travis CI가 실행중인 위치에서 생성

(4) ```mv freelec-springboot2-webservice.zip deploy/freelec-springboot2-webservice.zip```
- freelec-springboot2-webservice.zip 파일을 deploy/freelec-springboot2-webservice.zip으로 이동

(5)```local_dir: deploy```
- 앞에서 생성한 deploy 디렉토리를 지정
- 해당 위치의 파일 들만 S3로 전송합니다.


### Travis CI와 AWS S3, CodeDeploy 연동하기

### EC2와 CodeDeploy 연동
EC2가 CodeDeploy를 연동 받을 수 있게 EC2에서 사용할 IAM 역할 생성.

S3와 마찬가지로 IAM검색 합니다. 이번에는 사용자가 아닌 역할 을 선택한다. IAM > 역할 만들기
- 역할
  - AWS 서비스에만 할당할 수 있는 권한
  - EC2, CodeDeploy, SQS 등
  
- 사용자
  - AWS 서비스 외에 사용할 수 있는 권한
  - 로컬 pc, IDC 서버 등
  
지금 만들 권한은 EC2에서 사용할 것 이므로 역할 만들기에서 서비스 선택를 AWS 서비스 > EC2 으로 차례로 선택. 정책에선 EC2RoleForA 검색 > AmazonEC2RoleforAWS-CodeDeploy 선택. 태그는 본인이 원하는 이름으로 짓는다. 마지막으로 역할의 이름을 등록하고 나머지 등록 정보를 최종적으로 확인.

이렇게 만든 역할을 EC2 서비스에 등록. EC2 인스턴스 목록으로 이동한 뒤, 본인의 인스턴스를 마우스 오른쪽 클릭 > 인스턴스 설정 > IAM 역할 연결/바꾸기 차례로 선택한다. 그리고 방금 생성한 역할을 선택하고 인스턴스를 재부팅해준다.


### EC2 서버에 CodeDeploy 에이전트 설치
재부팅이 완료되었으면 CodeDeploy의 요청을 받을 수 있게 EC2 서버에 에이전트를 하나 설치한다.

```
aws s3 cp s3://aws-codedeploy-ap-northeast-2/latest/install .--region ap-northeast-2
...(내려받기 성공 후 메시지)
download: s3://aws-codedeploy-ap-northeast-2/latest/install to ./install

# 아래 명령어를 이어서 수행한다.
chmod +x ./install // 실행 권한 추가

sudo ./ install auto # install 파일로 설치를 진행

sudo service codedeploy-agent status # 실행 확인

Ths AWS CodeDeploy agent is running as PID xxx # 이 메시지가 나오면 정상이다
```

### CodeDeploy -> EC2 접근을 위해 CodeDeploy에서 사용할 IAM 역할 생성

CodeDeploy에서 EC2에 접근하려면 마찬가지로 권한이 필요하다. AWS의 서비스이니 IAM 역할을 생성한다. IAM > 역할 만들기 > AWS 서비스 > CodeDeploy 선택. CodeDeploy의 권한은 하나뿐이라서 선택 없이 바로 다음으로 넘어간다. 태그 역시 본인이 원하는 이름으로 짓는다.

### CodeDeploy 생성
CodeDeploy는 AWS의 배포 삼형제 중 하나

- Code Commit
  - 깃허브와 같은 코드 저장소의 역할을 한다.
  - 프라이빗 기능을 지원한다는 강점이 있지만, 현재 깃허브에서 무료로 프라이빗 지원을 하고 있어서 거의 사용되지 않는다.
  
- Code Build
  - Travis CI 와 마찬가지로 빌드용 서비스.
  - 멀티 모듈을 배포해야 하는 경우 사용해 볼만하지만, 규모가 있는 서비스에서는 대부분 젠킨스/팀시티 등을 이용 하니 이것 역시 사용할 일이 거의 없다.
  
- Code Deploy
  - AWS의 배포 서비스입니다.
  - 앞에서 언급한 다른 서비스들은 대체재가 있고, 딱히 대체재보다 나은 점이 없지만, CodeDeploy는 대체재가 없다.
  - 오토 스케일링 그룹 배포, 블루 그린 배포, 롤링 배포, EC2 단독 배포 등 많은 기능을 지원한다.
  
현재 프로젝트에서는 Code Commit의 역할은 github가, Code Build의 역할은 Travis CI 가 하고있다.

서비스 > CodeDeploy 서비스로 이동 해서 애플리케이션 생성 버튼 을 클릭한다.

- 애플리케이션 이름을 입력.
- 컴퓨팅 플랫폼은 EC2/온프레미스 를 선택

생성이 완료되면 배포 그룹을 생성하라는 메시지를 볼 수 있다. 배포 그룹 생성 버튼을 클릭.

- 배포 그룹 이름을 입력.
- 서비스 역할 선택은 방금 생성한 IAM 역할을 선택.
- 배포 유형은 현재 위치를 선택.
  - 배포할 서비스가 2대 이상이라면 블루/그린을 선택하면 된다.
- 환경 구성에는 Amazon EC2 인스턴스 에 체크한다.
- 배포 구성을 CodeDeployDefault.AllAtOnce 를 선택하고 로드밸런싱은 체크 해제한다.

여기 까지 CodeDeploy 설정은 끝이 났다. 이제 Travis CI와 CodeDeploy를 연동해 보겠다.

### CodeDeploy 관련 설정을 프로젝트의 appspec.yml에 추가
먼저 S3에서 넘겨줄 zip파일을 저장할 디렉토리를 EC2 서버에 하나 생성한다.
```
mkdir ~/app/step2 && mkdir ~/app/step2/zip
```

Travis CI의 Build가 끝나면 S3에 zip 파일이 전송되고, 이 zip 파일은 /home/ec2-user/app/step2/zip로 복사되어 압축을 풀 예정입니다.

AWS CodeDeploy 설정을 위해 프로젝트에 appspec.yml 파일을 추가 합니다.

- appspec.yml

```
version: 0.0 # (1)
os: linux
files:
  - source: / # (2)
    destination: /home/ec2-user/app/step2/zip/ # (3)
    overwrite: yes # (4)
```

(1) ```version```
- CodeDeploy 버전을 이야기합니다.
- 프로젝트 버전이 아니므로 0.0외에 다른 버전을 사용하면 오류가 발생한다.

(2) ```source```
- CodeDeploy에서 전달해 준 파일 중 destination으로 이동시킬 대상을 지정한다.
- 루트 경로(/)를 지정하면 전체 파일을 이야기한다.

(3)```destination```
- source에서 지정된 파일을 받을 위치
- 이후 jar를 실행하는 등은 destination에서 옮긴 파일들로 진행된다.

(4)```overwrite```
- 기존에 들이 있으면 덮어쓸지를 결정한다.
- 현재 yes라고 했으니 파일들을 덮어쓰게 된다.

### Travis CI 설정 파일(.travis.yml)에 CodeDeploy 내용을 추가

- .travis.yml
```
deploy:
  ...

  - provider: codedeploy
    access_key_id: $AWS_ACCESS_KEY # Travis repo setting에 설정된 값
    secret_access_key: $AWS_SECRET_KEY # Travis repo setting에 설정된 값

    bucket: freelec-springboot-doop-build # S3 버킷
    key: freelec-springboot2-webservice.zip # 빌드 파일을 압축해서 전달
    bundle_type: zip # 압축 확장자
    application: freelec-springboot2-webservice # 웹 콘솔에서 등록한 CodeDeploy 애플리케이션

    deployment_group: freelec-springboot2-webservice-group # 웹솔에서 등록한 CodeDeploy 배포 그룹

    region: ap-northeast-2
    wait-until-deployed: true
```

모든 내용을 작성했다면 깃 허브에 커밋/ 푸시를 하고 CodeDeploy 에서 배포가 수행되는 것을 확인할 수 있다. 또한 cd /home/ec2-user/app/step2/zip 경로에서 프로젝트 파일들이 잘 도착했음을 볼 수 있다.


### 배포 자동화 구성(스크립트 파일(.sh) 작성)

앞의 과정으로 Travis CI와 S3, CodeDeploy가 연동이 완료되었습니다. 이제 이것을 기반으로 실제로 Jar를 배포하여 실행 까지 해보겠습니다.

먼저 step 2 환경에서 실행될 deploy 스크립트 파일을 생성하겠습니다. 프로젝트 src 디렐토리와 같은 위치에 scripts 디렉토리를 생성해서 여기에 스크립트를 생성 합니다.

- deploy 스크립트 파일(.sh)
```
#!/bin/bash

REPOSITORY=/home/ec2-user/app/step2
PROJECT_NAME=freelec-springboot2-webservice

echo "> Build 파일 복사"

cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 현재 구동 중인 애플리케이션 pid 확인"

CURRENT_PID=$(pgrep -fl freelec-springboot2-webservice | grep jar | awk '{print $1}') # (1)

echo "현재 구동 중인 애플리케이션pid: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "> 새 애플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME # (2)

echo "> $JAR_NAME 실행"

nohup java -jar \
    -Dspring.config.location=classpath:/application.properties,classpath:/application-real.properties,/home/ec2-user/app/application-oauth.properties,/home/ec2-user/app/application-real-db.properties \
    -Dspring.profiles.active=real \
    $JAR_NAME > $REPOSITORY/nohup.out 2>&1 & # (3)
```

(1) 


