package gettothepoint.unicatapi.domain.repository;

import gettothepoint.unicatapi.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository <Project, Long> {
}
