package gettothepoint.unicatapi.domain.repository.storage;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalRepository.class})
class LocalRepositoryTest {

    @Autowired
    LocalRepository localRepository;

    @ParameterizedTest
    @ValueSource(strings = {
            "samples/image/sample.png",
            "samples/audio/sample01.mp3",
            "samples/transition/transition01.mp3"
    })
    void saveAndFindFileTest(String classpathResource) {
        ClassLoader classLoader = getClass().getClassLoader();
        var resourceUrl = classLoader.getResource(classpathResource);
        assertNotNull(resourceUrl, "리소스가 클래스패스에서 발견되지 않았습니다: " + classpathResource);
        File originalFile = new File(resourceUrl.getFile());
        assertTrue(originalFile.exists(), "리소스 파일이 존재해야 합니다");

        Path savedPath = localRepository.save(originalFile);
        assertNotNull(savedPath);

        Optional<File> foundFile = localRepository.findFileByRelativePath(savedPath);
        assertTrue(foundFile.isPresent());
        assertTrue(foundFile.get().exists());
    }
}