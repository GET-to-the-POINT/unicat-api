package gettothepoint.unicatapi.filestorage.application.port.in;

import gettothepoint.unicatapi.filestorage.application.exception.FileUploadErrorCode;
import gettothepoint.unicatapi.filestorage.application.exception.FileUploadException;
import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import gettothepoint.unicatapi.filestorage.domain.model.StoredFile;
import gettothepoint.unicatapi.filestorage.infrastructure.command.StoredFileFactory;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 업로드 유스케이스 테스트")
class FileUploadUseCaseTest {

    private static final Path DEFAULT_PATH = Path.of("");
    private static final Path CUSTOM_PATH = Path.of("test/path");
    private static final String GENERATED_FILE_ID = UUID.randomUUID().toString();
    private static final byte[] TEST_CONTENT = "테스트 파일 컨텐츠".getBytes();

    @Mock
    private FileStorageRepository repository;

    @InjectMocks
    private FileUploadUseCase useCase;

    @Nested
    @DisplayName("MultipartFile 업로드 테스트")
    class MultipartFileUploadTest {
        
        @Test
        @DisplayName("기본 경로로 업로드 성공")
        void shouldUploadWithDefaultPath() {
            // Given
            MultipartFile file = createValidMultipartFile();
            StoredFile mockStoredFile = mock(StoredFile.class);
            
            try (MockedStatic<StoredFileFactory> factory = mockStatic(StoredFileFactory.class)) {
                factory.when(() -> StoredFileFactory.fromMultipartFile(any(MultipartFile.class), eq(DEFAULT_PATH)))
                       .thenReturn(mockStoredFile);
                       
                given(repository.store(mockStoredFile)).willReturn(GENERATED_FILE_ID);
                
                // When
                String fileId = useCase.uploadFile(file);
                
                // Then
                assertThat(fileId)
                    .as("반환된 파일 ID는 저장소에서 생성된 ID와 동일해야 함")
                    .isEqualTo(GENERATED_FILE_ID);
                
                then(repository).should().store(mockStoredFile);
                then(repository).shouldHaveNoMoreInteractions();
                
                factory.verify(() -> StoredFileFactory.fromMultipartFile(file, DEFAULT_PATH), times(1));
            }
        }
        
        @Test
        @DisplayName("사용자 지정 경로로 업로드 성공")
        void shouldUploadWithCustomPath() {
            // Given
            MultipartFile file = createValidMultipartFile();
            StoredFile mockStoredFile = mock(StoredFile.class);
            
            try (MockedStatic<StoredFileFactory> factory = mockStatic(StoredFileFactory.class)) {
                factory.when(() -> StoredFileFactory.fromMultipartFile(any(MultipartFile.class), eq(CUSTOM_PATH)))
                       .thenReturn(mockStoredFile);
                       
                given(repository.store(mockStoredFile)).willReturn(GENERATED_FILE_ID);
                
                // When
                String fileId = useCase.uploadFile(file, CUSTOM_PATH);
                
                // Then
                assertThat(fileId)
                    .as("커스텀 경로 사용 시에도 동일한 파일 ID가 반환되어야 함")
                    .isEqualTo(GENERATED_FILE_ID);
                
                then(repository).should().store(mockStoredFile);
                factory.verify(() -> StoredFileFactory.fromMultipartFile(file, CUSTOM_PATH), times(1));
            }
        }
        
        @ParameterizedTest(name = "케이스: {0}")
        @MethodSource("invalidMultipartFileProvider")
        @DisplayName("유효하지 않은 MultipartFile 업로드")
        void shouldRejectInvalidMultipartFile(String testCase, MultipartFile invalidFile, FileUploadErrorCode expectedErrorCode) {
            // When & Then
            assertThatThrownBy(() -> useCase.uploadFile(invalidFile))
                .as("유효하지 않은 파일(%s)은 예외를 발생시켜야 함", testCase)
                .isInstanceOf(FileUploadException.class)
                .extracting("errorCode")
                .isEqualTo(expectedErrorCode);
                
            then(repository).shouldHaveNoInteractions();
        }
        
        static Stream<Arguments> invalidMultipartFileProvider() {
            return Stream.of(
                Arguments.of(
                    "비어있는 파일", 
                    createMultipartFile(true, 10L), 
                    FileUploadErrorCode.EMPTY_FILE
                ),
                Arguments.of(
                    "0 바이트 파일", 
                    createMultipartFile(false, 0L), 
                    FileUploadErrorCode.EMPTY_FILE
                )
            );
        }
    }
    
    @Nested
    @DisplayName("File 업로드 테스트")
    class FileUploadTest {
        
        @Test
        @DisplayName("일반 File 업로드 성공")
        void shouldUploadFile() {
            // Given
            File file = createValidFile();
            StoredFile mockStoredFile = mock(StoredFile.class);
            
            try (MockedStatic<StoredFileFactory> factory = mockStatic(StoredFileFactory.class)) {
                factory.when(() -> StoredFileFactory.fromFile(file, DEFAULT_PATH))
                       .thenReturn(mockStoredFile);
                       
                given(repository.store(mockStoredFile)).willReturn(GENERATED_FILE_ID);
                
                // When
                String fileId = useCase.uploadFile(file);
                
                // Then
                assertThat(fileId)
                    .as("파일 업로드 성공 시 저장소가 반환한 ID와 일치해야 함")
                    .isEqualTo(GENERATED_FILE_ID);
                
                then(repository).should().store(mockStoredFile);
                factory.verify(() -> StoredFileFactory.fromFile(file, DEFAULT_PATH));
            }
        }
        
        @Test
        @DisplayName("여러 파일 유효성 검사 실패 케이스")
        void shouldRejectInvalidFiles() {
            // 1. 존재하지 않는 파일
            File nonExistentFile = mock(File.class);
            given(nonExistentFile.exists()).willReturn(false);
            
            // 2. 읽을 수 없는 파일
            File unreadableFile = mock(File.class);
            given(unreadableFile.exists()).willReturn(true);
            given(unreadableFile.length()).willReturn(100L);
            given(unreadableFile.canRead()).willReturn(false);
            
            // 3. 빈 파일
            File emptyFile = mock(File.class);
            given(emptyFile.exists()).willReturn(true);
            given(emptyFile.length()).willReturn(0L);
            
            // When & Then
            SoftAssertions softly = new SoftAssertions();
            
            softly.assertThatThrownBy(() -> useCase.uploadFile(nonExistentFile))
                  .as("존재하지 않는 파일은 INVALID_FILE 예외를 발생시켜야 함")
                  .isInstanceOf(FileUploadException.class)
                  .extracting("errorCode")
                  .isEqualTo(FileUploadErrorCode.INVALID_FILE);
                  
            softly.assertThatThrownBy(() -> useCase.uploadFile(unreadableFile))
                  .as("읽을 수 없는 파일은 INVALID_FILE 예외를 발생시켜야 함")
                  .isInstanceOf(FileUploadException.class)
                  .extracting("errorCode")
                  .isEqualTo(FileUploadErrorCode.INVALID_FILE);
                  
            softly.assertThatThrownBy(() -> useCase.uploadFile(emptyFile))
                  .as("빈 파일은 INVALID_FILE 예외를 발생시켜야 함")
                  .isInstanceOf(FileUploadException.class)
                  .extracting("errorCode")
                  .isEqualTo(FileUploadErrorCode.INVALID_FILE);
            
            softly.assertAll();
            
            then(repository).shouldHaveNoInteractions();
        }
    }
    
    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionHandlingTest {
        
        @Test
        @DisplayName("팩토리 예외는 그대로 전파되어야 함")
        void shouldPropagateFactoryExceptions() {
            // Given
            MultipartFile file = createValidMultipartFile();
            RuntimeException factoryEx = new RuntimeException("팩토리 오류");
            
            try (MockedStatic<StoredFileFactory> factory = mockStatic(StoredFileFactory.class)) {
                factory.when(() -> StoredFileFactory.fromMultipartFile(any(), any()))
                       .thenThrow(factoryEx);
                
                // When & Then
                assertThatThrownBy(() -> useCase.uploadFile(file))
                    .as("팩토리에서 발생한 예외가 그대로 전파되어야 함")
                    .isSameAs(factoryEx);
                    
                then(repository).shouldHaveNoInteractions();
            }
        }
        
        @Test
        @DisplayName("저장소 예외는 그대로 전파되어야 함")
        void shouldPropagateStorageExceptions() {
            // Given
            MultipartFile file = createValidMultipartFile();
            StoredFile mockStoredFile = mock(StoredFile.class);
            RuntimeException storageEx = new RuntimeException("저장소 오류");
            
            try (MockedStatic<StoredFileFactory> factory = mockStatic(StoredFileFactory.class)) {
                factory.when(() -> StoredFileFactory.fromMultipartFile(any(), any()))
                       .thenReturn(mockStoredFile);
                       
                given(repository.store(any())).willThrow(storageEx);
                
                // When & Then
                assertThatThrownBy(() -> useCase.uploadFile(file))
                    .as("저장소에서 발생한 예외가 그대로 전파되어야 함")
                    .isSameAs(storageEx);
            }
        }
        
        @ParameterizedTest
        @NullSource
        @DisplayName("null 파일 처리 검증")
        void shouldRejectNullFiles(Object nullValue) {
            // When & Then
            assertThatNullPointerException()
                .as("null 파일은 NPE를 발생시켜야 함")
                .isThrownBy(() -> {
                    if (nullValue == null) {
                        useCase.uploadFile((MultipartFile) null);
                    }
                });
                
            assertThatNullPointerException()
                .as("null 파일은 NPE를 발생시켜야 함")
                .isThrownBy(() -> {
                    if (nullValue == null) {
                        useCase.uploadFile((File) null);
                    }
                });
                
            then(repository).shouldHaveNoInteractions();
        }
    }
    
    // 테스트 유틸리티 메서드
    
    private static MultipartFile createValidMultipartFile() {
        return new MockMultipartFile(
            "testFile",
            "test.txt",
            "text/plain", 
            TEST_CONTENT
        );
    }
    
    private static MultipartFile createMultipartFile(boolean isEmpty, long size) {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(isEmpty);
        given(file.getSize()).willReturn(size);
        return file;
    }
    
    private static File createValidFile() {
        File file = mock(File.class);
        given(file.exists()).willReturn(true);
        given(file.length()).willReturn((long) TEST_CONTENT.length);
        given(file.canRead()).willReturn(true);
        return file;
    }
}