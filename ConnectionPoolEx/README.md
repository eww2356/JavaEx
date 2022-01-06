# ConnectionPoolEx

커낵션 풀 객체를 생성하고 하나의 연결을 가져와서 사용 후 반환하기

##### 테스트 환경

* JAVA : 8 버전
* MariaDB : 10.3 버전
* Connector : mariadb-java-client-2.5.3.jar

##### MariaDB Connection 모니터링 sql

```
SHOW VARIABLES WHERE variable_name IN (
	'max_connections',	# 현재 설정된 최대 동시 연결 수
	'max_user_connections' # 계정당 생성할 수 있는 최대 동시 연결 수
);
```
```
SHOW STATUS WHERE variable_name IN (
	'max_used_connections',	# 동시 최대 접속자 수
	'aborted_clients', # 연결된 상태에서 강제로 연결 해제 된 연결 수
	'aborted_connects', # 연결 과정 중 fail된 연결 수
	'threads_connected', # 현재 오픈된 연결 수
	'connections' # 연결 시도된 총 수
);
```


참고 자료 : https://docs.microsoft.com/ko-kr/azure/mysql/sample-scripts-java-connection-pooling
