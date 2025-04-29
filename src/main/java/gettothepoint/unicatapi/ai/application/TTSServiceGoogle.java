package gettothepoint.unicatapi.ai.application;

import com.google.cloud.texttospeech.v1.*;
import gettothepoint.unicatapi.common.util.FileUtil;
import gettothepoint.unicatapi.filestorage.application.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;

@Primary
@Log4j2
@Service
@RequiredArgsConstructor
public class TTSServiceGoogle implements TTSService {

    private final TextToSpeechClient textToSpeechClient;
    private final FileService fileService;

    @Override
    public File create(String script, String voiceModel) {

        log.info("TTS 요청: {}", script);
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(script)
                .build();

        log.info("TTS 음성 모델: {}", voiceModel);
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setName(voiceModel)
                .build();

        log.info("TTS 음성 모델: {}", voiceModel);
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        log.info("TTS 요청 시작");
        log.info("TTS 요청: {}, {}, {}", input, voice, audioConfig);
        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

        byte[] audioBytes = response.getAudioContent().toByteArray();
        try (InputStream inputStream = new ByteArrayInputStream(audioBytes)) {
            Path filePath = FileUtil.getHashedFilePath(inputStream, ".mp3");
            File outputFile = filePath.toFile();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                log.info("TTS 파일 생성: {}", outputFile.getAbsolutePath());
                response.getAudioContent().writeTo(fos);
                return outputFile;
            }
        } catch (IOException e) {
            log.error("TTS 파일 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("TTS 파일 생성 중 오류가 발생했습니다.", e);
        }
    }
}
