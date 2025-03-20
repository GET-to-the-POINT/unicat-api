# 도움닫기

## 코드 클립보드에 넣기

클리보드로 복사할 디렉토리로 먼저 이동합니다.

```shell
cd /path/to/your/directory
```

### 하위 경로를 포함한 모든 파일을 클립보드로 복사

현재 디렉토리에서 하위 경로를 포함한 모든 파일을 클립보드로 복사합니다.

```shell
find . -type f -name "*.java" -exec cat {} + | tr -d '\n' | tr -d ' ' | pbcopy
```

윈도우 버전

```shell
(Get-ChildItem -Recurse -Filter *.java | Get-Content -Raw) -replace "[\r\n ]", "" | Set-Clipboard
```

application|domain|presentation 이것들의 파일만 추출 윈도우버전 

```shell
Get-ChildItem -Directory | Where-Object { $_.Name -match '^(application|domain|presentation)$' }
```

### 특정 디렉토리는 제외하고 클립보드로 복사

현제 디렉토리에서 특정 디렉토리는 제외하고 하위 경로를 포함한 모든 파일을 클립보드로 복사합니다.

```shell
find . -type f -name "*.java" -not -path "./application/*" -exec cat {} + | tr -d '\n' | tr -d ' ' | pbcopy
```
