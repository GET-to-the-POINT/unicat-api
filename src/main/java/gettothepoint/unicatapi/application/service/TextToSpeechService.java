package gettothepoint.unicatapi.application.service;

import com.google.cloud.texttospeech.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TextToSpeechService {

    private final TextToSpeechClient textToSpeechClient;

    public TextToSpeechService() throws IOException {
        this.textToSpeechClient = TextToSpeechClient.create();
    }

    public File create(String script, String voiceModel) {
        String voiceName = voiceModel;
        if (!StringUtils.hasText(voiceModel)) {
            voiceName = "ko-KR-Neural2-A";
        }
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(script)
                .build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                 .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

        File outputFile;
        try {
            outputFile = File.createTempFile("tts-", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                response.getAudioContent().writeTo(fos);
            }
        } catch (IOException e) {
            throw new RuntimeException("TTS 파일 생성 중 오류가 발생했습니다.", e);
        }

        return outputFile;
    }
}
