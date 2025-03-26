package gettothepoint.unicatapi.application.service.media;

import gettothepoint.unicatapi.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_imgcodecs;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.bytedeco.opencv.global.opencv_core.cvFlip;
import static org.bytedeco.opencv.global.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.opencv.global.opencv_imgcodecs.cvSaveImage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final String FILE_PREFIX = "unicat_artifact_";
    private static final String VIDEO_CODEC = "libx264";

    private static final String TRANSITION_AUDIO_CLASSPATH = "assets/audio/transition/transition1.mp3";
    private static final String TRANSITION_AUDIO_PREFIX = "transition";

    public static class MediaProcessingException extends RuntimeException {
        public MediaProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    public File mergeImageAndAudio(File imageFile, File soundFile) {
        File outputFile = FileUtil.createTempFile(FILE_PREFIX + "img_audio", ".mp4");
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(imageFile);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), 2)) {

            grabber.start();
            recorder.setVideoCodec(VIDEO_CODEC);
            recorder.setFrameRate(30);
            recorder.setFormat("mp4");
            recorder.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
            Mat mat = matConverter.convert(converter.convert(grabber.grabImage()));

            recorder.record(matConverter.convert(mat));
            recorder.stop();
            grabber.stop();
        } catch (IOException e) {
            throw new MediaProcessingException("Error merging image and audio", e);
        }
        return outputFile;
    }

    @Override
    public File mergeImageAndAudio(File templateResource, File contentResource, File audioResource) {
        File outputFile = FileUtil.createTempFile(FILE_PREFIX + "merged_with_bg_", ".mp4");
        try (FFmpegFrameGrabber templateGrabber = new FFmpegFrameGrabber(templateResource);
             FFmpegFrameGrabber contentGrabber = new FFmpegFrameGrabber(contentResource);
             FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(audioResource);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, templateGrabber.getImageWidth(), templateGrabber.getImageHeight(), 2)) {

            templateGrabber.start();
            contentGrabber.start();
            audioGrabber.start();
            recorder.setVideoCodec(VIDEO_CODEC);
            recorder.setFrameRate(30);
            recorder.setFormat("mp4");
            recorder.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
            Mat templateMat = matConverter.convert(converter.convert(templateGrabber.grabImage()));
            Mat contentMat = matConverter.convert(converter.convert(contentGrabber.grabImage()));

            recorder.record(matConverter.convert(templateMat));
            recorder.record(matConverter.convert(contentMat));
            recorder.stop();
            templateGrabber.stop();
            contentGrabber.stop();
            audioGrabber.stop();
        } catch (IOException e) {
            throw new MediaProcessingException("Error merging image and audio", e);
        }
        return outputFile;
    }

    @Override
    public File mergeImageAndAudio(File templateResource, File contentResource, File titleResource, File audioResource) {
        File outputFile = FileUtil.createTempFile(FILE_PREFIX + "merged_with_bg_", ".mp4");
        try (FFmpegFrameGrabber templateGrabber = new FFmpegFrameGrabber(templateResource);
             FFmpegFrameGrabber contentGrabber = new FFmpegFrameGrabber(contentResource);
             FFmpegFrameGrabber titleGrabber = new FFmpegFrameGrabber(titleResource);
             FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(audioResource);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, templateGrabber.getImageWidth(), templateGrabber.getImageHeight(), 2)) {

            templateGrabber.start();
            contentGrabber.start();
            titleGrabber.start();
            audioGrabber.start();
            recorder.setVideoCodec(VIDEO_CODEC);
            recorder.setFrameRate(30);
            recorder.setFormat("mp4");
            recorder.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
            Mat templateMat = matConverter.convert(converter.convert(templateGrabber.grabImage()));
            Mat contentMat = matConverter.convert(converter.convert(contentGrabber.grabImage()));
            Mat titleMat = matConverter.convert(converter.convert(titleGrabber.grabImage()));

            recorder.record(matConverter.convert(templateMat));
            recorder.record(matConverter.convert(contentMat));
            recorder.record(matConverter.convert(titleMat));
            recorder.stop();
            templateGrabber.stop();
            contentGrabber.stop();
            titleGrabber.stop();
            audioGrabber.stop();
        } catch (IOException e) {
            throw new MediaProcessingException("Error merging image and audio", e);
        }
        return outputFile;
    }

    @Override
    public File mergeVideosAndExtractVFR(List<File> files) {
        File outputFile = FileUtil.createTempFile(FILE_PREFIX + "merged_videos_", ".mp4");
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1920, 1080, 2)) {
            recorder.setVideoCodec(VIDEO_CODEC);
            recorder.setFrameRate(30);
            recorder.setFormat("mp4");
            recorder.start();

            for (File file : files) {
                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
                    grabber.start();
                    while (grabber.grab() != null) {
                        recorder.record(grabber.grab());
                    }
                    grabber.stop();
                }
            }
            recorder.stop();
        } catch (IOException e) {
            throw new MediaProcessingException("Error merging videos", e);
        }
        return outputFile;
    }

    @Override
    public File extractThumbnail(File file) {
        if (MediaValidationUtil.hasValidImageExtension(file.getName())) {
            return file;
        }
        File outputImage = FileUtil.createTempFile("thumbnail_", ".jpg");
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
            Mat mat = matConverter.convert(converter.convert(grabber.grabImage()));
            opencv_imgcodecs.imwrite(outputImage.getAbsolutePath(), mat);
            grabber.stop();
        } catch (IOException e) {
            throw new MediaProcessingException("Error extracting thumbnail", e);
        }
        return outputImage;
    }
}
