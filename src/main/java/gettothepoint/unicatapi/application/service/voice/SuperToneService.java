package gettothepoint.unicatapi.application.service.voice;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Primary
@Service
@Log4j2
public class SuperToneService implements TTSService {

    private final String ttsPath;
    private final String apiKey;
    private final String defaultVoiceId;
    private final RestTemplate restTemplate;

    public SuperToneService(@Value("${app.supertone.api-key}") String apiKey,
                            @Value("${app.supertone.default-voice-id}") String defaultVoiceId,
                            @Value("${app.supertone.temp-dir}") String ttsFilePath,
                            RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.defaultVoiceId = defaultVoiceId;
        this.ttsPath = FileUtil.getTempPath() + ttsFilePath;
        this.restTemplate = restTemplate;
    }

    @Override
    public File create(String script, String voiceModel) {
        String actualVoiceId = (voiceModel != null && !voiceModel.isEmpty()) ? voiceModel : defaultVoiceId;
        String url = "https://supertoneapi.com/v1/text-to-speech/" + actualVoiceId + "?output_format=mp3";

        // 요청 바디 생성
        Map<String, Object> requestBody = Map.of(
                "text", script,
                "language", "ko",
                "model", "turbo",
                "voice_settings", Map.of(
                        "pitch_shift", 0,
                        "pitch_variance", 1,
                        "speed", 1
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-sup-api-key", apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<byte[]> response =  restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            String errorMsg = "TTS 요청 실패, 상태 코드: " + response.getStatusCode();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 출력 디렉토리 생성
        new File(ttsPath).mkdirs();

        String outputFilePath = ttsPath + System.currentTimeMillis() + ".mp3";
        try {
            Files.write(Paths.get(outputFilePath), response.getBody());
        } catch (IOException e) {
            throw new RuntimeException("TTS 파일 저장 실패", e);
        }

        return new File(outputFilePath);
    }
}