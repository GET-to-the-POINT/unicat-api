package gettothepoint.unicatapi.application.service.media;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface MediaService {
    File mergeImageAndSoundFromFile(File imageFile, File soundFile);
    InputStream mergeImageAndSoundFromInputStream(InputStream imageStream, InputStream soundStream);

    File mergeVideosAndExtractVFRFromFiles(List<File> files);
    InputStream mergeVideosAndExtractVFRFromInputStream(List<InputStream> files);
}
