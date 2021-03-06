package com.sparta.week6project.controller;

import com.sparta.week6project.dto.requestDto.DuplicationRequestDto;
import com.sparta.week6project.dto.requestDto.LoginRequestDto;
import com.sparta.week6project.dto.requestDto.SignUpRequestDto;
import com.sparta.week6project.dto.responseDto.LoginResponseDto;
import com.sparta.week6project.dto.responseDto.SignUpResponseDto;
import com.sparta.week6project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/signup")
    public ResponseEntity<SignUpResponseDto> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
        return ResponseEntity.ok().body(userService.registerUser(signUpRequestDto));
    }

    @PostMapping("/user/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response){
        return ResponseEntity.ok().body(userService.loginUser(loginRequestDto));

    }
    @PostMapping("/user/signup/duplicate")
    public ResponseEntity<SignUpResponseDto> duplicationCheck(@Valid @RequestBody DuplicationRequestDto duplicationRequestDto){
        return ResponseEntity.ok().body(userService.duplicationCheck(duplicationRequestDto));
    }

//    @GetMapping("/user/logout")
//    public void logoutUser(){
//
//    }
}
