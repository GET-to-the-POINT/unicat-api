package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
