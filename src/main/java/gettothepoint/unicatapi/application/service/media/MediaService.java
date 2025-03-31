package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.domain.entity.dashboard.Section;

import java.io.File;
import java.util.List;

public interface MediaService {
    File mergeImageAndAudio(File imageFile, File soundFile);
    File mergeImageAndAudio(File templateResource, File contentResource, File audioResource);
    File mergeImageAndAudio(File templateResource, File contentResource, File titleResource, File audioResource);
    File mergeVideosAndExtractVFR(List<File> vieods, List<File> transitionSounds);
    File extractThumbnail(File videoFile);
}