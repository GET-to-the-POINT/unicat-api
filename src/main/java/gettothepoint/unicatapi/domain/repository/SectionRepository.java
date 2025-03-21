package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.dashboard.Project;
import gettothepoint.unicatapi.domain.entity.dashboard.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByProjectIdAndId(Long projectId, Long sectionId);
    Page<Section> findAllByProjectIdOrderBySortOrderAsc(Long projectId, Pageable pageable);
    List<Section> findAllByProjectIdOrderBySortOrderAsc(Long projectId);

    @Query("select coalesce(max(s.sortOrder), 0) from Section s where s.project.id = :projectId")
    Long findMaxSortOrderByProject(Long projectId);
}
