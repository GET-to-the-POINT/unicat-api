package gettothepoint.unicatapi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SwaggerSortConfig {

    @Bean
    public OpenApiCustomizer customSortingOpenApiCustomizer() {
        return openApi -> {
            sortTags(openApi);
            sortPathsAndOperations(openApi);
        };
    }

    private void sortTags(OpenAPI openApi) {
        if (openApi.getTags() == null) {
            return;
        }
        final List<String> customOrder = List.of("Auth", "Member", "Project", "Section", "Subscription", "Payment");
        openApi.setTags(openApi.getTags().stream().sorted((t1, t2) -> {
            int i1 = customOrder.indexOf(t1.getName());
            int i2 = customOrder.indexOf(t2.getName());
            if (i1 == -1) i1 = Integer.MAX_VALUE;
            if (i2 == -1) i2 = Integer.MAX_VALUE;
            return Integer.compare(i1, i2);
        }).toList());
    }

    private void sortPathsAndOperations(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }
        // 경로를 정렬하고, 새로운 Paths 인스턴스에 저장
        Map<String, PathItem> sortedPathsMap = openApi.getPaths().entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        Paths sortedPaths = new Paths();
        sortedPaths.putAll(sortedPathsMap);

        // HTTP 메서드 정렬 기준 정의
        Map<PathItem.HttpMethod, Integer> methodOrder = Map.of(PathItem.HttpMethod.GET, 1, PathItem.HttpMethod.POST, 2, PathItem.HttpMethod.PUT, 3, PathItem.HttpMethod.DELETE, 4, PathItem.HttpMethod.PATCH, 5, PathItem.HttpMethod.OPTIONS, 6, PathItem.HttpMethod.HEAD, 7, PathItem.HttpMethod.TRACE, 8);

        // 각 경로에 대해 오퍼레이션 정렬 실행
        sortedPaths.forEach((path, pathItem) -> sortOperations(pathItem, methodOrder));
        openApi.setPaths(sortedPaths);
    }

    private void sortOperations(PathItem pathItem, Map<PathItem.HttpMethod, Integer> methodOrder) {
        var operationsMap = pathItem.readOperationsMap();
        if (operationsMap == null || operationsMap.isEmpty()) {
            return;
        }
        var sortedOperations = operationsMap.entrySet().stream().sorted(Comparator.comparingInt(entry -> methodOrder.getOrDefault(entry.getKey(), Integer.MAX_VALUE))).toList();
        // 정렬된 순서대로 오퍼레이션을 재설정
        for (var entry : sortedOperations) {
            switch (entry.getKey()) {
                case GET -> pathItem.setGet(entry.getValue());
                case POST -> pathItem.setPost(entry.getValue());
                case PUT -> pathItem.setPut(entry.getValue());
                case DELETE -> pathItem.setDelete(entry.getValue());
                case PATCH -> pathItem.setPatch(entry.getValue());
                case OPTIONS -> pathItem.setOptions(entry.getValue());
                case HEAD -> pathItem.setHead(entry.getValue());
                case TRACE -> pathItem.setTrace(entry.getValue());
                default -> {
                }
            }
        }
    }
}