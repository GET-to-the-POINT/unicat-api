package gettothepoint.unicatapi.presentation.controller;

import gettothepoint.unicatapi.application.service.storage.FileStorageService;
import gettothepoint.unicatapi.application.service.storage.SampleVoiceService;
import gettothepoint.unicatapi.domain.dto.storage.SampleVoice;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final FileStorageService fileStorageService;
    private final SampleVoiceService sampleVoiceService;

    @PostMapping(consumes = "multipart/form-data")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        return fileStorageService.uploadFile(file);
    }

    @GetMapping("/voices")
    public SampleVoice[] getVoices() {
        return sampleVoiceService.getSampleVoices();
    }
}
