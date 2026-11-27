package maria.wicopt.user_service.exception;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode code;

    public ApiException(ErrorCode code) {
        super(code.getTitle());
        this.code = code;
    }

    public ApiException(ErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }
}