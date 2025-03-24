package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.application.service.TextToSpeechService;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.storage.AssetItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(
        classes = {AssetServiceImpl.class, AppProperties.class, AssetServiceImplTest.TestConfig.class},
        properties = {
                "app.supabase.url=https://bhqvrnbzzqzqlwwrcgbm.supabase.co",
                "app.supabase.key="
        }
)
@EnableConfigurationProperties(AppProperties.class)
class AssetServiceImplTest {

    @Autowired
    private AssetServiceImpl assetService;

    @Autowired
    private AppProperties appProperties;

    @Test
    void 템플릿_샘플_가져오기() {
        List<AssetItem> templates = assetService.get();
        assertThat(templates).isNotNull();
        assertThat(templates).isNotEmpty();
        templates.forEach(item -> {
            System.out.println("템플릿 이름: " + item.name());
            System.out.println("템플릿 URL: " + item.url());
            assertThat(item.url()).contains(appProperties.supabase().url());
        });
    }
    
    @TestConfiguration
    public static class TestConfig {
        @Bean
        public TextToSpeechService textToSpeechService() {
            // 여기서 필요에 따라 목(mock) 객체를 생성하거나 간단한 stub을 반환합니다.
            return mock(TextToSpeechService.class);
        }
    }
}
