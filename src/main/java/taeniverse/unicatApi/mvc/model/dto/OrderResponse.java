package taeniverse.unicatApi.mvc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long memberId;
    private String orderId;
    private String message;
}
