package gettothepoint.unicatapi.filestorage.application.port.in;

import gettothepoint.unicatapi.filestorage.application.exception.FileDownloadErrorCode;
import gettothepoint.unicatapi.filestorage.application.exception.FileDownloadException;
import gettothepoint.unicatapi.filestorage.application.port.out.FileStorageRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 다운로드 유스케이스 테스트")
class FileDownloadUseCaseTest {

    private static final String TEST_FILE_PATH = "file:/tmp/test-file.txt";
    private static final String VALID_KEY = "valid-file-key";
    private static final String NON_EXISTENT_KEY = "non-existent-key";

    @Mock
    private FileStorageRepository repository;

    @InjectMocks
    private FileDownloadUseCase useCase;

    @Nested
    @DisplayName("파일 다운로드 성공 케이스")
    class SuccessCases {
        @Test
        @DisplayName("유효한 파일 키로 파일 다운로드 성공")
        void shouldDownloadFileWithValidKey() throws MalformedURLException {
            // Given
            UrlResource expectedResource = new UrlResource(TEST_FILE_PATH);
            given(repository.load(VALID_KEY)).willReturn(Optional.of(expectedResource));

            // When
            Optional<UrlResource> result = useCase.downloadFile(VALID_KEY);

            // Then
            assertThat(result)
                    .as("파일이 존재해야 함")
                    .isPresent()
                    .hasValueSatisfying(resource -> {
                        assertThat(resource).isSameAs(expectedResource);
                        assertThat(resource.getFilename()).isEqualTo(expectedResource.getFilename());
                    });
            
            then(repository).should().load(VALID_KEY);
            then(repository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 파일 키로 다운로드 시 빈 Optional 반환")
        void shouldReturnEmptyOptionalForNonExistentKey() {
            // Given
            given(repository.load(NON_EXISTENT_KEY)).willReturn(Optional.empty());

            // When
            Optional<UrlResource> result = useCase.downloadFile(NON_EXISTENT_KEY);

            // Then
            assertThat(result)
                    .as("파일이 존재하지 않으므로 결과는 비어있어야 함")
                    .isEmpty();
            
            then(repository).should().load(NON_EXISTENT_KEY);
            then(repository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("파일 키 유효성 검증 케이스")
    class ValidationCases {
        @ParameterizedTest
        @NullSource
        @DisplayName("null 키로 다운로드 시 NPE 발생")
        void shouldThrowNPEForNullKey(String nullKey) {
            // When & Then
            assertThatNullPointerException()
                    .isThrownBy(() -> useCase.downloadFile(nullKey));
            
            then(repository).shouldHaveNoInteractions();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("빈 문자열 또는 공백 키로 다운로드 시 예외 발생")
        void shouldThrowExceptionForEmptyOrBlankKey(String invalidKey) {
            // When & Then
            assertThatThrownBy(() -> useCase.downloadFile(invalidKey))
                    .isInstanceOf(FileDownloadException.class)
                    .hasFieldOrPropertyWithValue("errorCode", FileDownloadErrorCode.INVALID_FILE_KEY);
            
            then(repository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("예외 처리 케이스")
    class ExceptionCases {
        @Test
        @DisplayName("저장소에서 발생한 예외는 그대로 전파")
        void shouldPropagateRepositoryException() {
            // Given
            RuntimeException expectedEx = new RuntimeException("저장소 접근 오류");
            given(repository.load(VALID_KEY)).willThrow(expectedEx);

            // When & Then
            assertThatThrownBy(() -> useCase.downloadFile(VALID_KEY))
                    .isInstanceOf(RuntimeException.class)
                    .isSameAs(expectedEx)
                    .hasMessage("저장소 접근 오류");
            
            then(repository).should().load(VALID_KEY);
        }
        
        @Test
        @DisplayName("다양한 저장소 예외 타입 처리 확인")
        void shouldHandleVariousExceptionTypes() {
            // Given
            IllegalStateException stateEx = new IllegalStateException("상태 오류");
            given(repository.load(anyString())).willThrow(stateEx);
            
            // When & Then
            SoftAssertions softly = new SoftAssertions();
            
            softly.assertThatThrownBy(() -> useCase.downloadFile("key1"))
                  .isInstanceOf(IllegalStateException.class)
                  .hasMessage("상태 오류");
            
            softly.assertAll();
        }
    }
}