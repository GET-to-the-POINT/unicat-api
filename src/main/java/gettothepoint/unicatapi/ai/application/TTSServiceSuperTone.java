package gettothepoint.unicatapi.ai.application;

import gettothepoint.unicatapi.common.properties.SupertoneProperties;
import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class TTSServiceSuperTone implements TTSService {

    private final SupertoneProperties supertoneProperties;
    private final RestTemplate restTemplate;

    @Override
    public File create(String script, String voiceModel) {
        String actualVoiceId = (voiceModel != null && !voiceModel.isEmpty()) ? voiceModel : supertoneProperties.defaultVoiceId();
        String url = "https://supertoneapi.com/v1/text-to-speech/" + actualVoiceId + "?output_format=mp3";

        // 요청 바디 생성
        Map<String, Object> requestBody = Map.of("text", script, "language", "ko", "model", "turbo", "voice_settings", Map.of("pitch_shift", 0, "pitch_variance", 1, "speed", 1.2));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-sup-api-key", supertoneProperties.apiKey());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            String errorMsg = "TTS 요청 실패, 상태 코드: " + response.getStatusCode();
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        try (InputStream inputStream = new ByteArrayInputStream(response.getBody())) {
            Path hashedPath = FileUtil.getHashedFilePath(inputStream, ".mp3");
            Files.write(hashedPath, response.getBody());
            return hashedPath.toFile();
        } catch (IOException e) {
            throw new RuntimeException("TTS 파일 저장 실패", e);
        }
    }
}