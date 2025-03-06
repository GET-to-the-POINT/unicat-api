package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.Videos;
import java.util.Optional;

public interface VideosRepository extends JpaRepository<Videos, Long> {
    Optional<Videos> findByVideoId(Long videoId);
}