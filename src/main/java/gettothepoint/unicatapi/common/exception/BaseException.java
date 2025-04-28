package gettothepoint.unicatapi.common.exception;

import lombok.Getter;

/**
 * 애플리케이션 전체에서 사용되는 기본 예외 클래스
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    protected BaseException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }
    
    protected BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    protected BaseException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.formatMessage(args), cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 에러 코드와 메시지를 포함한 상세 메시지를 반환합니다.
     * @return 포맷팅된 에러 메시지
     */
    @Override
    public String getMessage() {
        return String.format("[%s] %s", errorCode.getCode(), super.getMessage());
    }
}
