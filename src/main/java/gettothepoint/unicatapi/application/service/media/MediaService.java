package gettothepoint.unicatapi.application.service.media;

import java.io.File;
import java.util.List;

public interface MediaService {
    File mergeImageAndAudio(File imageFile, File soundFile);

    File mergeVideosAndExtractVFR(List<File> files);
}
