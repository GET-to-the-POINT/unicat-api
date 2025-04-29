package gettothepoint.unicatapi.artifact.persistence;

import gettothepoint.unicatapi.artifact.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository <Project, Long> {
}
