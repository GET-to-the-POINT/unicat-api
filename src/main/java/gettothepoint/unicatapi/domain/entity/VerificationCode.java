package gettothepoint.unicatapi.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes")
@Getter
@NoArgsConstructor

public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String email;


    private String code;


    private LocalDateTime expiresTime;

   @Builder
    public VerificationCode(String email, String code, LocalDateTime expiresTime) {
        this.email = email;
        this.code = code;
        this.expiresTime = expiresTime;
    }
}