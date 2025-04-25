package gettothepoint.unicatapi.email.domain.exception;

/**
 * 이메일 전송 실패 시 발생하는 예외
 */
public class MailSendException extends RuntimeException {
    public MailSendException(String message) {
        super(message);
    }

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
