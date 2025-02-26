package taeniverse.unicatApi.mvc.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taeniverse.unicatApi.mvc.model.dto.SignDto;
import taeniverse.unicatApi.mvc.service.AuthService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody SignDto signDto, HttpServletResponse response) {
        return authService.signUp(signDto, response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody SignDto signDto, HttpServletResponse response) {
        return authService.signIn(signDto, response);
    }
}