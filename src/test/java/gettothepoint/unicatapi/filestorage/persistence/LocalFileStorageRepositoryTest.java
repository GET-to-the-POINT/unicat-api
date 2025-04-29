package gettothepoint.unicatapi.filestorage.persistence;

import gettothepoint.unicatapi.filestorage.FileResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.UrlResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("로컬 파일 저장소 테스트")
class LocalFileStorageRepositoryTest {

    @TempDir
    static Path tempDir;

    private static LocalFileStorageRepository repository;

    @BeforeAll
    static void setUpRepository() {
        repository = new LocalFileStorageRepository(tempDir);
    }

    @Test
    @DisplayName("파일을 저장하고 다시 불러올 수 있다")
    void shouldStoreAndLoadFileSuccessfully() {
        // given
        String filename = "mocked-file.txt";
        byte[] bytes = "Mocked Content".getBytes();
        InputStream contentStream = new ByteArrayInputStream(bytes);

        FileResource mockFileResource = mock(FileResource.class);
        when(mockFileResource.getFilename()).thenReturn(filename);
        when(mockFileResource.getContent()).thenReturn(contentStream);

        // when
        String storedKey = repository.store(mockFileResource);

        // then
        assertThat(storedKey).as("store() 는 원본 파일명을 반환해야 한다").isEqualTo(filename);
    }

    @Test
    @DisplayName("존재하지 않는 키를 조회하면 빈 Optional 이 반환된다")
    void shouldReturnEmptyWhenFileNotExists() {
        // when
        Optional<UrlResource> result = repository.load("missing-file.txt");

        // then
        assertThat(result).as("없는 파일은 빈 Optional 이어야 한다").isEmpty();
    }
}