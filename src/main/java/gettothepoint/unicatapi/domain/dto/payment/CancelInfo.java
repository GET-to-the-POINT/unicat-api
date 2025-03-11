package gettothepoint.unicatapi.domain.dto.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
public class CancelInfo {
    private OffsetDateTime canceledAt;
    private String cancelReason;
    @JsonCreator
    public CancelInfo(
            @JsonProperty("canceledAt") OffsetDateTime canceledAt,
            @JsonProperty("cancelReason") String cancelReason) {
        this.canceledAt = canceledAt;
        this.cancelReason = cancelReason;
    }
}