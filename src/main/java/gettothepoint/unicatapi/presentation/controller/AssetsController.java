package gettothepoint.unicatapi.presentation.controller;

import gettothepoint.unicatapi.application.service.storage.SampleVoiceService;
import gettothepoint.unicatapi.domain.dto.storage.SampleVoice;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Project - Asset", description = "Assets API")
@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetsController {

    private final SampleVoiceService sampleVoiceService;

    @GetMapping("/voices")
    public SampleVoice[] getVoices() {
        return sampleVoiceService.getSampleVoices();
    }
}
