package taeniverse.unicatApi.mvc.service;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taeniverse.unicatApi.constant.Role;
import taeniverse.unicatApi.mvc.model.dto.SignDTO;
import taeniverse.unicatApi.mvc.model.entity.User;
import taeniverse.unicatApi.mvc.repository.UserRepository;

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
