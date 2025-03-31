package gettothepoint.unicatapi.application.service.media;

import java.io.File;
import java.util.List;

public interface MediaService {
    File mergeImageAndAudio(File templateResource, File contentResource, File audioResource);
    File mergeImageAndAudio(File templateResource, File contentResource, File titleResource, File audioResource);
    File mergeVideosAndExtractVFR(List<File> vieods, List<File> transitionSounds);
}