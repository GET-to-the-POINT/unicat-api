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

    @JsonCreator
    public CancelInfo(@JsonProperty("canceledAt") OffsetDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }
}
