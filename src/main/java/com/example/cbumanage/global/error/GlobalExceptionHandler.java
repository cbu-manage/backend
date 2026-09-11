package com.example.cbumanage.global.error;

import com.example.cbumanage.global.common.ApiResponse;
import com.example.cbumanage.member.exception.MemberDoesntHavePermissionException;
import com.example.cbumanage.member.exception.MemberNotExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiResponse<>(errorCode.getCode(), e.getMessage(), null));
    }

    @ExceptionHandler(MemberNotExistsException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMemberNotExistsException(MemberNotExistsException e) {
        return ResponseEntity
                .status(ErrorCode.USER_NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.USER_NOT_FOUND));
    }

    @ExceptionHandler(MemberDoesntHavePermissionException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMemberDoesntHavePermissionException(MemberDoesntHavePermissionException e) {
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getHttpStatus())
                .body(new ApiResponse<>(ErrorCode.FORBIDDEN.getCode(), e.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ErrorCode.INVALID_REQUEST.getCode(), message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_REQUEST));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + ": 요청 값의 형식이 올바르지 않습니다.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ErrorCode.INVALID_REQUEST.getCode(), message, null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntity
                .status(ErrorCode.UPLOAD_SIZE_EXCEEDED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.UPLOAD_SIZE_EXCEEDED));
    }

    /* 라우팅 오류까지 아래 Exception 핸들러로 떨어지면 오타 하나에도 500이 나간다 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    protected ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(Exception e) {
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.NOT_FOUND));
    }

    /* 필수 파라미터 누락도 아래 Exception 핸들러로 떨어지면 500이 나가 실제 장애와 구분되지 않는다 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMissingRequestParameter(MissingServletRequestParameterException e) {
        String message = e.getParameterName() + ": 필수 요청 파라미터입니다.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ErrorCode.INVALID_REQUEST.getCode(), message, null));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMissingRequestPart(MissingServletRequestPartException e) {
        String message = e.getRequestPartName() + ": 필수 요청 항목입니다.";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(ErrorCode.INVALID_REQUEST.getCode(), message, null));
    }

    /*
     * DB 제약(유니크 등)에 걸린 경우다. 같은 버튼을 두 번 눌러 같은 행을 두 번 만들려는 상황이 대부분이라
     * 500(장애)이 아니라 409로 알린다. 제약이 없으면 중복 행이 그대로 생겨 이후 조회가 영구히 깨지므로,
     * 이 핸들러는 DB 쪽 유니크 제약과 짝을 이룰 때 의미가 있다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e,
                                                                             HttpServletRequest request) {
        log.warn("무결성 제약 위반 {} {} — {}", request.getMethod(), request.getRequestURI(), e.getMostSpecificCause().getMessage());
        return ResponseEntity
                .status(ErrorCode.DUPLICATE_RESOURCE.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.DUPLICATE_RESOURCE));
    }

    /* 두 사람이 같은 자료를 동시에 저장하면 나중 요청이 조용히 덮어쓰지 않도록 409로 알린다 */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    protected ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(OptimisticLockingFailureException e) {
        return ResponseEntity
                .status(ErrorCode.CONCURRENT_MODIFICATION.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.CONCURRENT_MODIFICATION));
    }

    /*
     * 없는 리소스 조회는 사용자 실수지 장애가 아니다. 전역 매핑이 없어 62곳이 500으로 나가던 것을
     * 404로 통일한다. 컨트롤러에서 이미 손으로 catch 하는 곳은 그쪽이 먼저 처리하므로 영향 없다.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException e,
                                                                     HttpServletRequest request) {
        log.info("리소스 없음 {} {} — {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.NOT_FOUND));
    }

    /*
     * 여기로 떨어지는 예외는 전부 의도치 않은 장애다. 스택트레이스를 남기지 않으면
     * @ExceptionHandler가 예외를 삼켜 Spring 기본 로깅조차 찍히지 않아 원인 추적이 불가능하다.
     * 응답 본문은 그대로 두고(내부 정보 비노출) 서버 로그에만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("COMMON-001", "서버 내부 오류가 발생했습니다.", null));
    }
}
