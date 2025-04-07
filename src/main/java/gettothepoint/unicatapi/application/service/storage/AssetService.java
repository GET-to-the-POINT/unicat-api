// AssetService.java
package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.dto.asset.AssetItem;
import gettothepoint.unicatapi.domain.repository.storage.S3Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private static final String DEFAULT_BUCKET = "assets";
    private static final String TYPE_TEMPLATE = "template";
    private static final String TYPE_TRANSITION = "transition";
    private static final String TYPE_VOICE = "voice";

    private final S3Repository s3Repository; // 추가된 의존성

    public List<AssetItem> getAll() {
        List<AssetItem> assets = new ArrayList<>();
        assets.addAll(getAll(TYPE_TEMPLATE));
        assets.addAll(getAll(TYPE_TRANSITION));
        assets.addAll(getAll(TYPE_VOICE));
        return assets;
    }

    public List<AssetItem> getAll(String type) {
        String bucket = DEFAULT_BUCKET + "/" + type;
        return s3Repository.assets(bucket);
    }
}