package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.common.propertie.SupabaseProperties;
import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
import gettothepoint.unicatapi.domain.repository.SupabaseFileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final SupabaseProperties supabaseProperties;
    private final SupabaseFileStorageRepository supabaseFileStorageRepository;


    public static final String TYPE_TEMPLATE = "template";
    public static final String TYPE_TRANSITION = "transition";
    public static final String TYPE_VOICE = "voice";
    public static final String BUCKET_ASSETS = "assets";

    @Override
    public List<AssetItem> get() {
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

        ListObjectsV2Response response = supabaseFileStorageRepository.getFolderListInBucket(BUCKET_ASSETS, prefix);

        String publicEndpoint = supabaseProperties.s3().endpoint().replace("/s3", "");

        return response.contents().stream().map(s3Object -> {
            String name = s3Object.key().substring(prefix.length() + 1);
            String assetUrl = String.format("%s/object/%s/%s/%s", publicEndpoint, BUCKET_ASSETS, prefix, name);
            return new AssetItem(name, assetUrl);
        }).toList();
    }

    @Override
    public String get(String prefix, String fileName) {
        String publicEndpoint = supabaseProperties.s3().endpoint().replace("/s3", "");
        return String.format("%s/object/%s/%s/%s", publicEndpoint, BUCKET_ASSETS, prefix, fileName);
    }
}