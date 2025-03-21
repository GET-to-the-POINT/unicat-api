# 도움닫기

## 자주 쓰는 프롬프트

```shell
(echo "롤: 너는 자바 선생님이야

규칙: 최신 자바 문법을 알려주고 올른 SOLID 규칙으로 설계해줘
추가적으로 필요한 내용이 있다면 내게 다시 질문해 내가 답할게
비디오 머지 서비스와 그와 영상제작에 관련된 코드에 대해서 개선된 코드를 답해줘
"; \
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
