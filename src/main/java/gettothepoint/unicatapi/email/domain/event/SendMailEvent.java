package gettothepoint.unicatapi.email.domain.event;

import gettothepoint.unicatapi.email.domain.MailMessage;

public record SendMailEvent(MailMessage mailMessage) {
}
