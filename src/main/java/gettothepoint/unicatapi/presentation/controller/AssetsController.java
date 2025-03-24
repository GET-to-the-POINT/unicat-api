package gettothepoint.unicatapi.presentation.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import gettothepoint.unicatapi.application.service.storage.SampleVoiceService;
import gettothepoint.unicatapi.domain.dto.storage.SampleVoice;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
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

    @GetMapping("/imageStyles")
    public ImageStyle[] getImageStyles() {
        return ImageStyle.values();
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @Getter
    @RequiredArgsConstructor
    public enum ImageStyle {
        PHOTO("Photo", "실제 사진처럼 보이는 이미지"),
        ILLUSTRATION("Illustration", "일러스트"),
        PENCIL_SKETCH("Pencil sketch", "손그림"),
        DIGITAL_ART("Digital Art", "디지털 아트"),
        ANIME("Anime", "애니메이션"),
        WATERCOLOR("Watercolor", "수채화"),
        CONCEPT_ART("Concept Art", "컨셉 아트");

        private final String value;
        private final String name;
    }
}
