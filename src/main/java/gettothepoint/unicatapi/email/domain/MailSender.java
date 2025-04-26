package gettothepoint.unicatapi.email.domain;

/**
 * 이메일 전송을 위한 도메인 서비스 인터페이스
 */
public interface MailSender {
    /**
     * 이메일 메시지 전송
     * @param mailMessage 전송할 이메일 메시지
     */
    void send(MailMessage mailMessage);
}
