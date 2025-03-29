package gettothepoint.unicatapi.application.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.domain.dto.storage.AssetItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final SupabaseProperties supabaseProperties;
    private final RestTemplate restTemplate;
    private final MessageSource messageSource;

    public static final String TYPE_TEMPLATE = "template";
    public static final String TYPE_TRANSITION = "transition";
    public static final String TYPE_VOICE = "voice";
    public static final String BUCKET_ASSETS = "assets";

    @Override
    public List<AssetItem> get(){
            List<AssetItem> all = new ArrayList<>();
            all.addAll(get(TYPE_TEMPLATE));
            all.addAll(get(TYPE_TRANSITION));
            all.addAll(get(TYPE_VOICE));
            return all;
    }

    @Override
    public List<AssetItem> get(String type) {
        return switch (type) {
            case TYPE_TEMPLATE -> getSampleAssets(TYPE_TEMPLATE);
            case TYPE_TRANSITION -> getSampleAssets(TYPE_TRANSITION);
            case TYPE_VOICE -> getSampleAssets(TYPE_VOICE);
            default -> throw new IllegalArgumentException("지원하지 않는 타입입니다: " + type);
        };
    }


    private List<AssetItem> getSampleAssets(String prefix) {
        String url = getListUrl();
        String supabaseKey = supabaseProperties.key();

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody;
        try {
            ObjectNode bodyNode = objectMapper.createObjectNode()
                    .put("prefix", prefix + "/")
                    .put("search", "");

            ObjectNode sortByNode = objectMapper.createObjectNode();
            sortByNode.put("column", "name");
            sortByNode.put("order", "asc");
            bodyNode.set("sortBy", sortByNode);

            requestBody = objectMapper.writeValueAsString(bodyNode);
        } catch (Exception e) {
            throwInternalError();
            return List.of(); // 불필요하지만 예외처리용
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity;

        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (RestClientException e) {
            log.error("샘플 에셋 조회 중 오류 발생: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "샘플 에셋 목록을 가져오지 못했습니다.");
        }

        List<AssetItem> assetItems = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            for (JsonNode node : root) {
                String fileName = node.get("name").asText();
                String assetUrl = get(prefix, fileName);
                assetItems.add(new AssetItem(fileName, assetUrl));
            }
        } catch (Exception e) {
            throwInternalError();
        }

        return assetItems;
    }

    private void throwInternalError() {
        String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    private String getListUrl() {
        String supabaseUrl = supabaseProperties.url();
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", "list", AssetServiceImpl.BUCKET_ASSETS)
                .build()
                .toUriString();
    }

    @Override
    public String get(String prefix, String fileName) {
        String supabaseUrl = supabaseProperties.url();
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", "public", AssetServiceImpl.BUCKET_ASSETS, prefix, fileName)
                .build()
                .toUriString();
    }

    @Override
    public String getDefaultTemplateUrl() {
        final String DEFAULT_TEMPLATE_FILENAME = "back2.mp4";

        // Supabase의 기본 URL (supabaseProperties를 통해 가져온다고 가정)
        String supabaseUrl = supabaseProperties.url();

        // 예: https://your-supabase-url/storage/v1/object/assets/template/black-background.jpg
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", "assets", "template", DEFAULT_TEMPLATE_FILENAME)
                .build()
                .toUriString();
    }
}