package gettothepoint.unicatapi.email.domain;

import lombok.Builder;

@Builder
public record MailMessage(String recipient, String subject, String content, boolean isHtml) {
}