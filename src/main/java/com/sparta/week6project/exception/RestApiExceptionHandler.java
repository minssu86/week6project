package com.sparta.week6project.exception;


import com.sparta.week6project.dto.responseDto.ErrorResponseDto;
import com.sparta.week6project.dto.responseDto.LoginResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

// Controller에서 Exception이 발생했을 때 Json형태로 리턴해준다. <> @ResponseBody + @ConttrollerAdvice
// 이렇게 처리를 안하고 평소대로 던지기만하면 다 500에러로 출력되니까
@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(value = {
            IllegalArgumentException.class,
            NullPointerException.class,
            ResponseStatusException.class} )
    public ResponseEntity<Object> handlerControllerException (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto(false, e.getMessage()));
    }

    //validation exception
    // request body 유효성 검사는 MethodArgumentNotValidException
    // @Validated는 constrainViolationException
    //MethodArgumentNotValidException에서 BindingResult를 사용할 수 있는데,
    //BindingResult에는 Validaiton에서 걸린 변수의 message를 확인 가능
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> validException(MethodArgumentNotValidException e) {
//        ObjectError objectError=e.getBindingResult().getAllErrors().stream().findFirst().get();
        ErrorResponseDto ResponseException = new ErrorResponseDto(false, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return ResponseEntity.badRequest().body(ResponseException);
    }


    @ExceptionHandler({LoginIdNotValidException.class})
    public ResponseEntity<Object> handlerLoginIdNotValidException (LoginIdNotValidException e) {
        return ResponseEntity.badRequest().body(new LoginResponseDto(null, false, e.getMessage()));
    }

    @ExceptionHandler(LoginPasswordNotValidException.class)
    public ResponseEntity<Object> handlerLoginPasswordNotValidException (LoginPasswordNotValidException e) {
        return ResponseEntity.badRequest().body(new LoginResponseDto(null, false, e.getMessage()));
    }
}
