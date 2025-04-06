package gettothepoint.unicatapi.application.service.voice;

import com.google.cloud.texttospeech.v1.*;
import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Log4j2
@Service
@RequiredArgsConstructor
public class GoogleTextToSpeechService implements TTSService {

    private final TextToSpeechClient textToSpeechClient;

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

        File outputFile;
        try {
            log.info("TTS 응답: {}", response);
            outputFile = FileUtil.getUniqueFilePath(".mp3").toFile();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                log.info("TTS 파일 생성: {}", outputFile.getAbsolutePath());
                response.getAudioContent().writeTo(fos);
            }
        } catch (IOException e) {
            log.error("TTS 파일 생성 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("TTS 파일 생성 중 오류가 발생했습니다.", e);
        }

        return outputFile;
    }
}
