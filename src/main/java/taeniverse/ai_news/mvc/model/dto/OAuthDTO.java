package taeniverse.ai_news.mvc.model.dto;

import lombok.*;

@Getter
@Builder
public class OAuthDTO {

    private String role;
    private String name;
    private String username;
    private Long userId;

}
