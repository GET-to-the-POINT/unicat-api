package taeniverse.unicatApi.mvc.model.dto;

import lombok.Getter;

@Getter
public class UploadRequest {
    private String videoId;
    private String title;
    private String description;
}