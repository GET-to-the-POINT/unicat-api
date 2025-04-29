package gettothepoint.unicatapi.artifact.persistence;

import gettothepoint.unicatapi.artifact.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository <Project, Long> {
    @Query("SELECT DISTINCT p FROM Project p JOIN FETCH p.uploadVideo WHERE p.member.id = :memberId")
    List<Project> findProjectsWithUploadVideoByMemberId(@Param("memberId") Long memberId);
}
