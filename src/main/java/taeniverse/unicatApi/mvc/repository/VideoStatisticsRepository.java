package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taeniverse.unicatApi.mvc.model.entity.VideoStatistics;

@Repository
public interface VideoStatisticsRepository extends JpaRepository<VideoStatistics, Long> {

}