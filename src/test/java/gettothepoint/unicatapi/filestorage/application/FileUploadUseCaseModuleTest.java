package gettothepoint.unicatapi.filestorage.application;

import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageCommand;
import gettothepoint.unicatapi.filestorage.domain.storage.FileStorageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static gettothepoint.unicatapi.filestorage.config.CommonTestConfig.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 업로드 유스케이스 테스트")
class FileUploadUseCaseModuleTest {

    @Mock
    private FileStorageRepository fileStorageRepository;

    @InjectMocks
    private FileUploadUseCase fileUploadUseCase;

    @Test
    @DisplayName("파일 업로드 성공 테스트")
    void uploadFileShouldSucceed() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(TEST_CONTENT.getBytes()));
        when(mockFile.getSize()).thenReturn((long) TEST_CONTENT.length());
        when(mockFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);

        fileUploadUseCase.uploadFile(mockFile);

        ArgumentCaptor<FileStorageCommand> captor = ArgumentCaptor.forClass(FileStorageCommand.class);
        verify(fileStorageRepository, times(1)).store(captor.capture());
        FileStorageCommand command = captor.getValue();
        try (ByteArrayInputStream inputStream = (ByteArrayInputStream) command.content()) {
            assertArrayEquals(TEST_CONTENT.getBytes(), inputStream.readAllBytes());
            assertEquals(TEST_CONTENT.length(), command.size());
            assertEquals(TEST_CONTENT_TYPE, command.contentType());
        }
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생 테스트")
    void uploadFileShouldFailWhenFileIsEmpty() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("업로드할 파일이 비어 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("I/O 예외 발생 시 런타임 예외 발생 테스트")
    void uploadFileShouldFailWhenIOExceptionOccurs() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getInputStream()).thenThrow(new IOException("I/O error"));
        when(mockFile.getSize()).thenReturn((long) TEST_CONTENT.length());
        when(mockFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);

        assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

    }

    @Test
    @DisplayName("파일 저장소에 저장 실패 시 예외 발생 테스트")
    void uploadFileShouldFailWhenStorageFails() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(TEST_FILENAME);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(TEST_CONTENT.getBytes()));
        when(mockFile.getSize()).thenReturn((long) TEST_CONTENT.length());
        when(mockFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);

        doThrow(new RuntimeException("저장소 오류")).when(fileStorageRepository).store(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("저장소 오류", exception.getMessage());
    }

    @Test
    @DisplayName("파일 업로드 실패 테스트 - 확장자 조작 (허용되지 않는 확장자)")
    void uploadFileShouldFailWithDisallowedExtension() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(TEST_CONTENT.getBytes()));
        when(mockFile.getSize()).thenReturn((long) TEST_CONTENT.length());
        when(mockFile.getContentType()).thenReturn("application/x-sh");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fileUploadUseCase.uploadFile(mockFile);
        });

        assertEquals("파일의 MIME 타입이 일치하지 않습니다.", exception.getMessage());
    }
}
