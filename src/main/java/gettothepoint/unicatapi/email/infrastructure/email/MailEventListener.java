package gettothepoint.unicatapi.email.infrastructure.email;

import gettothepoint.unicatapi.email.domain.MailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailEventListener {

    private final SyncMailSender syncMailSender;

    @Async
    @EventListener
    public void handleSendMailEvent(MailMessage mailMessage) {
        try {
            syncMailSender.send(mailMessage);
            log.info("비동기 메일 전송 완료: {}", mailMessage.recipient());
        } catch (Exception e) {
            log.error("비동기 메일 전송 실패: {}", mailMessage.recipient(), e);
        }
    }
}
