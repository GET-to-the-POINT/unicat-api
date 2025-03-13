package gettothepoint.unicatapi.application.service;

import com.google.cloud.texttospeech.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TextToSpeechService {

    private final TextToSpeechClient textToSpeechClient;

    public TextToSpeechService() throws IOException {
        this.textToSpeechClient = TextToSpeechClient.create();
    }

    public byte[] createTextToSpeech(String text, String voiceName) {

        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(text)
                .build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setName(voiceName)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
        return response.getAudioContent().toByteArray();
    }
}
