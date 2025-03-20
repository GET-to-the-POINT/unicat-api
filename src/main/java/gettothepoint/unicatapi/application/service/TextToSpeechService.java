package gettothepoint.unicatapi.application.service;

import com.google.cloud.texttospeech.v1.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class TextToSpeechService {

    private final TextToSpeechClient textToSpeechClient;

    public TextToSpeechService() throws IOException {
        this.textToSpeechClient = TextToSpeechClient.create();
    }

    public void createAndSaveTTSFile(String text, String voiceName, String filePath) throws IOException {
        byte[] audioData = createTextToSpeech(text, voiceName);
        saveTTSFile(audioData, filePath);
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

    public void saveTTSFile(byte[] audioData, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(audioData);
        }
    }

    public InputStream create(String script, String voiceModel) {
        SynthesisInput input = SynthesisInput.newBuilder()
                .setText(script)
                .build();

        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("ko-KR")
                .setName(voiceModel)
                .build();

        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();

        SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
        return response.getAudioContent().newInput();
    }
}
