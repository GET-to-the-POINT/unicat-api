package gettothepoint.unicatapi.filestorage.domain.exception;

import lombok.Getter;

@Getter
public enum FileStorageErrorCode {
    EMPTY_FILENAME("파일명은 빈 값일 수 없습니다"),
    PATH_TRAVERSAL_DETECTED("경로 조작이 감지되었습니다: %s"),
    ABSOLUTE_PATH_DETECTED("절대 경로가 감지되었습니다: %s"),
    LEADING_DOT_FILENAME("파일명이 .으로 시작할 수 없습니다: %s"),
    MULTIPLE_DOTS_DETECTED("파일명에 ..이 포함될 수 없습니다: %s"),
    FORBIDDEN_CHARACTERS("파일명에 금지된 문자가 포함되어 있습니다: %s"),
    WINDOWS_SPECIAL_RULE_VIOLATION("Windows에서는 파일명이 마침표나 공백으로 끝날 수 없습니다: %s"),
    NON_POSITIVE_SIZE("파일 크기는 0보다 커야 합니다"),
    SIZE_MISMATCH("제공된 크기(%d)와 실제 크기(%d)가 일치하지 않습니다"),
    UNSUPPORTED_INPUTSTREAM("InputStream은 mark/reset을 지원해야 합니다"),
    UNSUPPORTED_EXTENSION("허용되지 않는 확장자입니다: %s"),
    EXTENSION_MIMETYPE_MISMATCH("확장자에 맞는 MIME 타입이 아님: 기대 %s, 실제 %s"),
    CONTENT_TYPE_MISMATCH("제공된 Content-Type과 감지된 MIME 타입이 일치하지 않습니다: 제공 %s, 감지 %s"),
    IO_ERROR("InputStream 검증 중 오류 발생");

    private final String messageTemplate;

    FileStorageErrorCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, args);
    }
}
