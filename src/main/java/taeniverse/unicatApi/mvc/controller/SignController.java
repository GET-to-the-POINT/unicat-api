package taeniverse.unicatApi.mvc.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import taeniverse.unicatApi.mvc.model.dto.sign.SignInDto;
import taeniverse.unicatApi.mvc.model.dto.sign.SignUpDto;
import taeniverse.unicatApi.mvc.service.AuthService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SignController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@Valid @RequestBody SignUpDto signUpDto, HttpServletResponse response) {
        authService.signUp(signUpDto, response);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public void signIn(@Valid @RequestBody SignInDto signInDto, HttpServletResponse response) {
        authService.signIn(signInDto, response);
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.OK)
    public void signOut(HttpServletResponse response) {
        authService.signOut(response);
    }
}