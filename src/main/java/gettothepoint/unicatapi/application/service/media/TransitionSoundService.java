//package gettothepoint.unicatapi.application.service.media;
//
//import gettothepoint.unicatapi.application.service.storage.StorageService;
//import gettothepoint.unicatapi.domain.entity.project.Section;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TransitionSoundService {
//
//    private final StorageService storageService;
//
//    public List<File> downloadTransitionSoundsFromSections(List<Section> sections) {
//        List<File> transitionSoundFiles = new ArrayList<>();
//
//        for (int i = 1; i < sections.size(); i++) {
//            Section section = sections.get(i);
//            String transitionKey = section.getTransitionKey();
//
//            if (StringUtils.hasText(transitionKey)) {
//                try {
//                    File soundFile = storageService.getFile(transitionKey);
//                    transitionSoundFiles.add(soundFile);
//                } catch (Exception e) {
//                    log.warn("⚠️ 트랜지션 사운드 다운로드 실패 (sectionId: {}, url: {}): {}", section.getId(), transitionKey, e.getMessage());
//                    transitionSoundFiles.add(null);
//                }
//            } else {
//                log.info("ℹ️ sectionId {} 는 트랜지션 사운드가 없음, null 추가", section.getId());
//                transitionSoundFiles.add(null);
//            }
//        }
//
//        return transitionSoundFiles;
//    }
//    public double getAudioDurationInSeconds(File file) {
//        try {
//            ProcessBuilder pb = new ProcessBuilder(
//                    "ffprobe", "-v", "error", "-show_entries", "format=duration",
//                    "-of", "default=noprint_wrappers=1:nokey=1",
//                    file.getAbsolutePath()
//            );
//            Process p = pb.start();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//                String line = reader.readLine();
//                p.waitFor();
//                return Double.parseDouble(line.trim());
//            }
//        } catch (Exception e) {
//            throw new MediaServiceImpl.MediaProcessingException("음성 길이 가져오기 실패", e);
//        }
//    }
//
//    public long getVideoDurationInMs(File file) {
//        try {
//            ProcessBuilder pb = new ProcessBuilder(
//                    "ffprobe", "-v", "error", "-select_streams", "v:0",
//                    "-show_entries", "format=duration",
//                    "-of", "default=noprint_wrappers=1:nokey=1",
//                    file.getAbsolutePath()
//            );
//            Process p = pb.start();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//                String line = reader.readLine();
//                p.waitFor();
//                return (long) (Double.parseDouble(line.trim()) * 1000);
//            }
//        } catch (Exception e) {
//            throw new MediaServiceImpl.MediaProcessingException("비디오 길이 가져오기 실패", e);
//        }
//    }
//}
