package com.ssafy.enjoytrip.core.support;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorCode;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("support")
class SupportUtilitiesTest {

    @Nested
    class ApiResponsesAndErrors {
        @DisplayName("성공 응답은 오류 없이 데이터를 담는다")
        @Test
        void successResponseCarriesDataWithoutError() {
            ApiResponse<String> response = ApiResponse.success("ok");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo("ok");
            assertThat(response.getError()).isNull();
        }

        @DisplayName("실패 응답은 안정적인 오류 코드와 메시지를 복사한다")
        @Test
        void failureResponseCopiesStableErrorCodeAndMessage() {
            ApiResponse<Void> response = ApiResponse.fail(ErrorType.INVALID_CREDENTIALS);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getData()).isNull();
            assertThat(response.getError().code()).isEqualTo(ErrorCode.UNAUTHORIZED);
            assertThat(response.getError().message())
                    .isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        @DisplayName("CoreException은 오류 타입과 원인을 보존한다")
        @Test
        void coreExceptionKeepsErrorTypeAndCause() {
            IllegalArgumentException cause = new IllegalArgumentException("잘못된 값입니다.");

            CoreException exception = new CoreException(ErrorType.INVALID_REQUEST, cause);

            assertThat(exception.errorType()).isEqualTo(ErrorType.INVALID_REQUEST);
            assertThat(exception).hasMessage("유효하지 않은 요청입니다.").hasCause(cause);
        }
    }
}
