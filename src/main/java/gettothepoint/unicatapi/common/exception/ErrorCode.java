package gettothepoint.unicatapi.common.exception;

/**
 * 모든 에러 코드가 구현해야 하는 인터페이스
 */
public interface ErrorCode {
    
    /**
     * 에러 코드 값을 반환합니다.
     * @return 에러 코드 값
     */
    String getCode();
    
    /**
     * 에러 메시지를 반환합니다.
     * @return 에러 메시지
     */
    String getMessage();
    
    /**
     * 포맷팅된 에러 메시지를 반환합니다.
     * @param args 메시지 포맷팅에 사용할 인자들
     * @return 포맷팅된 에러 메시지
     */
    default String formatMessage(Object... args) {
        if (args == null || args.length == 0) {
            return getMessage();
        }
        
        try {
            return String.format(getMessage(), args);
        } catch (Exception e) {
            return getMessage();
        }
    }
}
