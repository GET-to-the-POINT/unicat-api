package gettothepoint.unicatapi.application.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final Translate translate;

    public String translateToEnglish(String text) {
        Translation result = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage("ko"),
                Translate.TranslateOption.targetLanguage("en"),
                Translate.TranslateOption.model("nmt")
        );
        return result.getTranslatedText();
    }
}
