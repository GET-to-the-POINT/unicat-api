# 도움닫기

## 자주 쓰는 프롬프트

```shell
(echo "롤: 너는 자바 선생님이야
SOLID 원칙을 위배하는 코드를 알려주고 클린코드와 클린아키텍처의 시각으로 코드를 개선해줘
개선해야할 클래스: MediaServiceImpl
그리고 연관된 클래스도 같이 개선이 필요하면 알려줘
"; \
find \
src/main/java/gettothepoint/unicatapi/application/service \
src/main/java/gettothepoint/unicatapi/domain \
src/main/java/gettothepoint/unicatapi/common \
src/main/java/gettothepoint/unicatapi/presentation/controller \
src/main/java/gettothepoint/unicatapi/UnicatApiApplication.java \
-type f -name "*.java" -exec grep -v '^import ' {} +) \
| tr -d '\n' | tr -d ' ' | pbcopy
```

```shell
(echo "롤: 너는 자바 선생님이야

규칙: 최신 자바 문법을 알려주고 올른 SOLID 규칙으로 설계해줘
추가적으로 필요한 내용이 있다면 내게 다시 질문해 내가 답할게

나는 자바 개발자인데 초보 개발자야 세세하게 설명해줘야해"; \
find ./application ./presentation ./domain -type f -name "*.java" -exec cat {} +) \
| tr -d '\n' | tr -d ' ' | pbcopy
```

## 질문 프롬프트

```text
롤: 너는 자바 선생님이야

규칙: 최신 자바 문법을 알려주고 올른 SOLID 규칙으로 설계해줘
추가적으로 필요한 내용이 있다면 내게 다시 질문해 내가 답할게

나는 자바 개발자인데 초보 개발자야 세세하게 설명해줘야해
```

## 코드 클립보드에 넣기

클리보드로 복사할 디렉토리로 먼저 이동합니다.

## 디렉토리 구조

MacOS
```shell
find . -print | sed -e 's;[^/]*/;|____;g;s;____|; |;g'
find . -type d | sed -e 's;[^/]*/;|____;g;s;____|; |;g'
```

```shell
cd /path/to/your/directory
```

### 하위 경로를 포함한 모든 파일을 클립보드로 복사

현재 디렉토리에서 하위 경로를 포함한 모든 파일을 클립보드로 복사합니다.

```shell
find . -type f -name "*.java" -exec cat {} + | tr -d '\n' | tr -d ' ' | pbcopy
```

### 특정 디렉토리만 선택해서 클립보드로 복사

현재 디렉토리에서 특정 디렉토리만 선택해서 하위 경로를 포함한 모든 파일을 클립보드로 복사합니다.

```shell
find ./application ./presentation ./domain -type f -name "*.java" -exec cat {} + | tr -d '\n' | tr -d ' ' | pbcopy
```

### 특정 디렉토리는 제외하고 클립보드로 복사

현제 디렉토리에서 특정 디렉토리는 제외하고 하위 경로를 포함한 모든 파일을 클립보드로 복사합니다.

```shell
find . -type f -name "*.java" -not -path "./application/*" -exec cat {} + | tr -d '\n' | tr -d ' ' | pbcopy
```
