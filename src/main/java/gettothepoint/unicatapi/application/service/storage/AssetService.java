package gettothepoint.unicatapi.application.service.storage;

import gettothepoint.unicatapi.domain.dto.asset.AssetItem;

import java.util.List;

public interface AssetService {
    List<AssetItem> get();
    List<AssetItem> get(String type);
    String get(String type, String name);
}
