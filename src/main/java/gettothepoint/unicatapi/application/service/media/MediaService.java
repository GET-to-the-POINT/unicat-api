package gettothepoint.unicatapi.application.service.media;

import java.io.File;
import java.util.List;

public interface MediaService {
    File mergeImageAndAudio(File imageFile, File soundFile);
    File mergeImageAndAudio(File imageFile, File soundFile, File titleImageFile);


    File mergeVideosAndExtractVFR(List<File> files);

}
