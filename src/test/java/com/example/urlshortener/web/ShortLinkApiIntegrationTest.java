package com.example.urlshortener.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.example.urlshortener.config.TestAsyncConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
class ShortLinkApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRedirectAndStats() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "longUrl", "https://example.com/docs/spring-boot"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.longUrl").value("https://example.com/docs/spring-boot"))
                .andReturn();

        String body = create.getResponse().getContentAsString();
        String shortCode = objectMapper.readTree(body).get("shortCode").asText();

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/docs/spring-boot"));

        mockMvc.perform(get("/api/v1/links/" + shortCode + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(1))
                .andExpect(jsonPath("$.recordedEvents").value(1));
    }

    @Test
    void customAliasConflict() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "longUrl", "https://example.com/a",
                "customAlias", "mybrand"));

        mockMvc.perform(post("/api/v1/links").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/links").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("alias_conflict"));
    }
}
