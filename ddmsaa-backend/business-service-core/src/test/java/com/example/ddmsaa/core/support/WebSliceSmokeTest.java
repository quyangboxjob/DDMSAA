package com.example.ddmsaa.core.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.example.ddmsaa.common.enums.Role;
import com.example.ddmsaa.core.controller.PingController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** L2 基盤（{@link WebSliceTest}・{@link TestJwt}・{@link ApiResponseAssert}）の動作確認。 */
@WebSliceTest(controllers = PingController.class)
@DisplayName("L2 テスト基盤（Web スライス + 本物の JWT フィルタ）")
class WebSliceSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwt jwt;

    @Test
    @DisplayName("有効な JWT なら保護された API が 200 / Result 包絡で応答する")
    void validJwtReachesController() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/ping")
                .header(HttpHeaders.AUTHORIZATION, jwt.bearer("E0001", Role.EMPLOYEE)))
                .andReturn();

        ApiResponseAssert.assertThat(result).isSuccess().dataIs("pong");
    }

    @Test
    @DisplayName("JWT がなければ 401 / E0004 の ErrorResponse になる")
    void missingJwtIsUnauthorized() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/ping")).andReturn();

        ApiResponseAssert.assertThat(result).isError(401, "E0004");
    }

    @Test
    @DisplayName("期限切れの JWT は 401 になり、コントローラへ届かない")
    void expiredJwtIsUnauthorized() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/ping")
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwt.expiredToken("E0001", Role.EMPLOYEE)))
                .andReturn();

        org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("別の鍵で署名した JWT は 401 になる")
    void wrongSignatureIsUnauthorized() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/ping").header(HttpHeaders.AUTHORIZATION,
                "Bearer " + jwt.tokenWithWrongSignature("E0001", Role.EMPLOYEE))).andReturn();

        org.assertj.core.api.Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
}
