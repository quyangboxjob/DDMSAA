package com.example.ddmsaa.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ddmsaa.common.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@DisplayName("PingController の疎通確認")
class PingControllerTest {

    private final PingController controller = new PingController();

    @Test
    @DisplayName("ping は pong を Result でラップして返す")
    void pingReturnsPong() {
        Result<String> result = controller.ping();

        assertThat(result.data()).isEqualTo("pong");
        assertThat(result.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/ping は 200 と data=pong を返す")
    void getPing() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("pong"));
    }
}
