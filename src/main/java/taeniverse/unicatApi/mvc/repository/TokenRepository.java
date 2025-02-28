package taeniverse.unicatApi.mvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taeniverse.unicatApi.mvc.model.entity.Member;
import taeniverse.unicatApi.mvc.model.entity.OAuth2;

public interface TokenRepository extends JpaRepository<OAuth2, Long> {
    // 특정 username에 해당하는 Token 정보 조회
    OAuth2 findByUsername(String username);

    // 특정 member와 provider로 Token을 조회
    OAuth2 findByMemberAndProvider(Member member, String provider);
}
