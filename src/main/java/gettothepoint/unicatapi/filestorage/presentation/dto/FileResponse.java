package gettothepoint.unicatapi.filestorage.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileResponse {
    private String fileKey;
    private String message;
    
    public static FileResponse success(String fileKey) {
        return new FileResponse(fileKey, "파일이 성공적으로 업로드되었습니다.");
    }
}
