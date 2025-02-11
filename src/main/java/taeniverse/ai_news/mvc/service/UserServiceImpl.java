package taeniverse.ai_news.mvc.service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taeniverse.ai_news.constant.Role;
import taeniverse.ai_news.mvc.model.dto.SignDTO;
import taeniverse.ai_news.mvc.model.entity.User;
import taeniverse.ai_news.mvc.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public void signUp(SignDTO signDTO) {

        if (userRepository.existsByEmail(signDTO.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(signDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(signDTO.getPassword()))
                .role(Role.USER.name())
                .build();

        userRepository.save(user);

    }

}
