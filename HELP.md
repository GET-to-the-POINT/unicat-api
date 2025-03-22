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

## 코드 클립보드에 넣기

클리보드로 복사할 디렉토리로 먼저 이동합니다.

## 디렉토리 구조

MacOS
```shell
find . -print | sed -e 's;[^/]*/;|____;g;s;____|; |;g'
find . -type d | sed -e 's;[^/]*/;|____;g;s;____|; |;g'
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

윈도우 버전

```shell
(Get-ChildItem -Recurse -Filter *.java | Get-Content -Raw) -replace "[\r\n ]", "" | Set-Clipboard
```

application|domain|presentation 이것들의 파일만 추출 윈도우버전 

```shell
(Get-ChildItem -Directory |
    Where-Object Name -match '^(application|domain|presentation)$' |
    ForEach-Object { Get-ChildItem -Path $_.FullName -Recurse -Filter *.java } |
    Get-Content -Raw) -replace "[\r\n ]", "" |
    Set-Clipboard
```
트리 파일구조 

```shell
tree.com
```

### 특정 디렉토리는 제외하고 클립보드로 복사

현제 디렉토리에서 특정 디렉토리는 제외하고 하위 경로를 포함한 모든 파일을 클립보드로 복사합니다.

```shell
find . -type f -name "*.java" -not -path "./application/*" -exec cat {} + | tr -d '\n' | tr -d ' ' | pbcopy
```
