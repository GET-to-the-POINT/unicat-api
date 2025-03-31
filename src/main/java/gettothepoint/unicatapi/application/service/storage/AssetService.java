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

    public static final String TYPE_TEMPLATE = "template";
    public static final String TYPE_TRANSITION = "transition";
    public static final String TYPE_VOICE = "voice";

    private final S3Repository s3Repository; // 추가된 의존성

    public List<AssetItem> getAll() {
        List<AssetItem> assets = new ArrayList<>();
        assets.addAll(getAll(TYPE_TEMPLATE));
        assets.addAll(getAll(TYPE_TRANSITION));
        assets.addAll(getAll(TYPE_VOICE));
        return assets;
    }

    public List<AssetItem> getAll(String type) {
        String prefix = "assets";
        return s3Repository.listAssets(prefix);
    }
}