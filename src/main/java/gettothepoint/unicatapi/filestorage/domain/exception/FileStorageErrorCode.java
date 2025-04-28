package gettothepoint.unicatapi.filestorage.domain.exception;

import gettothepoint.unicatapi.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 스토리지 도메인에서 발생하는 오류 코드
 */
@Getter
@RequiredArgsConstructor
public enum FileStorageErrorCode implements ErrorCode {
    EMPTY_FILENAME("FS-D001", "파일명은 빈 값일 수 없습니다"),
    PATH_TRAVERSAL_DETECTED("FS-D002", "경로 조작이 감지되었습니다: %s"),
    ABSOLUTE_PATH_DETECTED("FS-D003", "절대 경로가 감지되었습니다: %s"),
    LEADING_DOT_FILENAME("FS-D004", "파일명이 .으로 시작할 수 없습니다: %s"),
    MULTIPLE_DOTS_DETECTED("FS-D005", "파일명에 ..이 포함될 수 없습니다: %s"),
    FORBIDDEN_CHARACTERS("FS-D006", "파일명에 금지된 문자가 포함되어 있습니다: %s"),
    WINDOWS_SPECIAL_RULE_VIOLATION("FS-D007", "Windows에서는 파일명이 마침표나 공백으로 끝날 수 없습니다: %s"),
    NON_POSITIVE_SIZE("FS-D008", "파일 크기는 0보다 커야 합니다"),
    SIZE_MISMATCH("FS-D009", "제공된 크기(%d)와 실제 크기(%d)가 일치하지 않습니다"),
    UNSUPPORTED_INPUTSTREAM("FS-D010", "InputStream은 mark/reset을 지원해야 합니다"),
    UNSUPPORTED_EXTENSION("FS-D011", "허용되지 않는 확장자입니다: %s"),
    EXTENSION_MIMETYPE_MISMATCH("FS-D012", "확장자에 맞는 MIME 타입이 아님: 기대 %s, 실제 %s"),
    CONTENT_TYPE_MISMATCH("FS-D013", "제공된 Content-Type과 감지된 MIME 타입이 일치하지 않습니다: 제공 %s, 감지 %s"),
    IO_ERROR("FS-D014", "InputStream 검증 중 오류 발생");

    private final String code;
    private final String message;
}
