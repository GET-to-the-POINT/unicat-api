package gettothepoint.unicatapi.presentation.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import gettothepoint.unicatapi.application.service.storage.AssetService;
import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
import gettothepoint.unicatapi.domain.repository.storage.S3Repository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Asset", description = "샘플 리소스(보이스, 배경화면, 효과음 등)를 가져오는 API")
@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetsController {

    private final AssetService assetService;
    private final S3Repository s3Repository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String test(@RequestParam("file") MultipartFile file) {
        return s3Repository.save(file);
    }

    @Operation(
            summary = "샘플 에셋 조회",
            description = """
                숏폼 영상을 만들 때 사용할 수 있는 샘플 리소스를 조회합니다.
             
                - voice: 텍스트를 음성으로 읽어주는 샘플 보이스 목록
                - transition: 영상 전환 시 사용할 수 있는 효과음
                - template: 영상 배경으로 사용할 수 있는 템플릿 배경화면
                - type이 없으면 전부 반환됩니다
                """
    )
    @GetMapping
    public List<AssetItem> getAssets(
            @Parameter(
                    description = "에셋 타입 (허용값: voice, transition, template)",
                    schema = @Schema(allowableValues = {"voice", "transition", "template"})
            )
            @RequestParam(required = false) String type
    ) {
        return type == null ? assetService.getAll() : assetService.getAll(type);
    }

    @GetMapping("/image-styles")
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
