package gettothepoint.unicatapi.application.service.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gettothepoint.unicatapi.common.propertie.AppProperties;
import gettothepoint.unicatapi.domain.dto.SampleVoice;
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
public class SampleVoiceService {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final MessageSource messageSource;

    public SampleVoice[] getSampleVoices() {
        String supabaseKey = appProperties.supabase().key();
        String bucket = "voice";
        String prefix = "sample";
        String url = getListUrl(bucket);

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
            String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (RestClientException e) {
            log.error("샘플 보이스 조회 중 오류 발생: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "샘플 보이스 목록을 가져오지 못했습니다.");
        }

        List<SampleVoice> voices = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseEntity.getBody());
            for (JsonNode node : root) {
                String fileName = node.get("name").asText();
                String voiceUrl = getPublicUrl(bucket, prefix, fileName);
                voices.add(new SampleVoice(fileName, voiceUrl));
            }
        } catch (Exception e) {
            String errorMessage = messageSource.getMessage("error.unknown", null, "", LocaleContextHolder.getLocale());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }

        return voices.toArray(new SampleVoice[0]);
    }

    private String getListUrl(String bucket) {
        String supabaseUrl = appProperties.supabase().url();
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", "list", bucket)
                .build()
                .toUriString();
    }

    private String getPublicUrl(String bucket, String prefix, String fileName) {
        String supabaseUrl = appProperties.supabase().url();
        return UriComponentsBuilder.fromUriString(supabaseUrl)
                .pathSegment("storage", "v1", "object", "public", bucket, prefix, fileName)
                .build()
                .toUriString();
    }

}

