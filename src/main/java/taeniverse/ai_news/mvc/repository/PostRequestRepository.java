package taeniverse.ai_news.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.ai_news.mvc.model.entity.PostRequest;

import java.time.LocalDate;
import java.util.List;

public interface PostRequestRepository extends JpaRepository<PostRequest, Long> {
    List<PostRequest> findByUserIdAndDate(Long userId, LocalDate date);

}
