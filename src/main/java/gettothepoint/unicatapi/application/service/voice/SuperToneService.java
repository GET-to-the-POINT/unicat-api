package gettothepoint.unicatapi.application.service.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import gettothepoint.unicatapi.common.util.FileUtil;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Primary
@Service
@Log4j2
public class SuperToneService implements TTSService {

    private final String ttsPath;
    private final String apiKey;
    private final String defaultVoiceId;

    public SuperToneService(@Value("${app.supertone.api-key}") String apiKey,
                            @Value("${app.supertone.default-voice-id}") String defaultVoiceId,
                            @Value("${app.supertone.temp-dir}") String ttsFilePath) {
        this.apiKey = apiKey;
        this.defaultVoiceId = defaultVoiceId;
        this.ttsPath = FileUtil.getTempPath() + ttsFilePath;
    }

    @Override
    public File create(String script, String voiceModel) {
        String actualVoiceId = (voiceModel != null && !voiceModel.isEmpty()) ? voiceModel : defaultVoiceId;
        String url = "https://supertoneapi.com/v1/text-to-speech/" + actualVoiceId + "?output_format=mp3";

        // JSON 요청 본문 구성
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> body = Map.of(
                "text", script,
                "language", "ko",
                "model", "turbo",
                "voice_settings", Map.of(
                        "pitch_shift", 0,
                        "pitch_variance", 1,
                        "speed", 1
                )
        );

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }

        HttpResponse<byte[]> response = Unirest.post(url).header("x-sup-api-key", apiKey).header("Content-Type", "application/json").body(jsonBody).asBytes();

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