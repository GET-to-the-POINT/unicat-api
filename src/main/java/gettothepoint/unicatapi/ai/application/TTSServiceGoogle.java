package gettothepoint.unicatapi.ai.application;

import com.google.cloud.texttospeech.v1.*;
import gettothepoint.unicatapi.filestorage.application.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Log4j2
@Service
@RequiredArgsConstructor
public class TTSServiceGoogle implements TTSService {

    private final TextToSpeechClient textToSpeechClient;
    private final FileService fileService;

    @Override
    public String create(String script, String voiceModel) {

        log.info("TTS 요청: {}", script);
        SynthesisInput input = SynthesisInput.newBuilder().setText(script).build();

        log.info("TTS 음성 모델: {}", voiceModel);
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("ko-KR").setName(voiceModel).build();

        log.info("TTS 음성 모델: {}", voiceModel);
        AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

        log.info("TTS 요청 시작");
        log.info("TTS 요청: {}, {}, {}", input, voice, audioConfig);
        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
        byte[] audioBytes = response.getAudioContent().toByteArray();
        String fileName = "/audio/tts-" + System.currentTimeMillis() + ".mp3";
        return fileService.store(fileName, audioBytes);
    }
}
