package gettothepoint.unicatapi.presentation.controller;

import gettothepoint.unicatapi.application.service.FileStorageService;
import gettothepoint.unicatapi.domain.dto.StorageUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = "multipart/form-data")
    public StorageUpload uploadFile(@RequestParam("file") MultipartFile file) {
        return fileStorageService.uploadFile(file);
    }
}
