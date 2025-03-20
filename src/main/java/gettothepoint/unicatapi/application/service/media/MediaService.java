package gettothepoint.unicatapi.application.service.media;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface MediaService {
    InputStream mergeImageAndSound(File imageFile, File soundFile);
    InputStream mergeImageAndSound(InputStream imageStream, InputStream soundStream);

    File mergeVideosAndExtractVFRFromFiles(List<File> files);
    InputStream mergeVideosAndExtractVFRFromInputStream(List<InputStream> files);
}
