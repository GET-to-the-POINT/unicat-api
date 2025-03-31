package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.application.service.voice.GoogleTextToSpeechService;
import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
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
        classes = {AssetServiceImpl.class, AssetServiceImplTest.TestConfig.class, SupabaseProperties.class},
        properties = {
                "app.supabase.url=https://bhqvrnbzzqzqlwwrcgbm.supabase.co",
                "app.supabase.key="
        }
)
@EnableConfigurationProperties(SupabaseProperties.class)
class AssetServiceImplTest {

    @Autowired
    private AssetServiceImpl assetService;

    @Autowired
    private SupabaseProperties supabaseProperties;

    @Test
    void 템플릿_샘플_가져오기() {
        List<AssetItem> templates = assetService.get();
        assertThat(templates).isNotNull();
        assertThat(templates).isNotEmpty();
        templates.forEach(item -> {
            System.out.println("템플릿 이름: " + item.name());
            System.out.println("템플릿 URL: " + item.url());
            assertThat(item.url()).contains(supabaseProperties.url());
        });
    }
    
    @TestConfiguration
    public static class TestConfig {
        @Bean
        public GoogleTextToSpeechService textToSpeechService() {
            // 여기서 필요에 따라 목(mock) 객체를 생성하거나 간단한 stub을 반환합니다.
            return mock(GoogleTextToSpeechService.class);
        }
    }
}
