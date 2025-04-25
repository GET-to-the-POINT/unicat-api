package gettothepoint.unicatapi.email.domain;

import gettothepoint.unicatapi.email.domain.exception.MailSendException;

/**
 * 이메일 전송을 위한 도메인 서비스 인터페이스
 */
public interface MailSender {
    /**
     * 이메일 메시지 전송
     * @param mailMessage 전송할 이메일 메시지
     * @throws MailSendException 이메일 전송 실패 시
     */
    void send(MailMessage mailMessage) throws MailSendException;
}
