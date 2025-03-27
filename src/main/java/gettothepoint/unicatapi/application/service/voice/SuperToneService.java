package gettothepoint.unicatapi.application.service.voice;

import gettothepoint.unicatapi.common.util.FileUtil;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Primary
@Service
@RequiredArgsConstructor
@Log4j2
public class SuperToneService implements TTSService {

    private String ttsPath = FileUtil.getTempPath() + "tts-";

    @Value("${app.supertone.api-key}")
    private String apiKey;

    @Value("${app.supertone.default-voice-id}")
    private String defaultVoiceId;

    public SuperToneService(String apiKey, String defaultVoiceId, String ttsFilePath) {
        this.apiKey = apiKey;
        this.defaultVoiceId = defaultVoiceId;
        if (StringUtils.hasText(ttsFilePath)) {
            this.ttsPath = ttsFilePath;
        }
    }

    @Override
    public File create(String script, String voiceModel) {
        String actualVoiceId = (voiceModel != null && !voiceModel.isEmpty()) ? voiceModel : defaultVoiceId;
        String url = "https://supertoneapi.com/v1/text-to-speech/" + actualVoiceId + "?output_format=mp3";

        // JSON 요청 본문 구성
        String jsonBody = "{\n" +
                "  \"text\": \"" + script + "\",\n" +
                "  \"language\": \"ko\",\n" +
                "  \"model\": \"turbo\",\n" +
                "  \"voice_settings\": {\n" +
                "    \"pitch_shift\": 0,\n" +
                "    \"pitch_variance\": 1,\n" +
                "    \"speed\": 1\n" +
                "  }\n" +
                "}";

        HttpResponse<byte[]> response = Unirest.post(url)
                .header("x-sup-api-key", apiKey)
                .header("Content-Type", "application/json")
                .body(jsonBody)
                .asBytes();

        if (response.getStatus() != 200) {
            log.error("TTS 요청 실패, 상태 코드: {}", response.getStatus());
            byte[] bodyBytes = response.getBody();
            String bodyText = new String(bodyBytes, StandardCharsets.UTF_8); // 인코딩 주의
            log.error("응답 본문: {}", bodyText);
            throw new RuntimeException("TTS 요청 실패, 상태 코드: " + response.getStatus());
        }

        // Ensure the output directory exists
        new File(ttsPath).mkdirs();

        String outputFilePath = ttsPath + System.currentTimeMillis() + ".mp3";

        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(outputFilePath), response.getBody());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }

        return new File(outputFilePath);
    }
}