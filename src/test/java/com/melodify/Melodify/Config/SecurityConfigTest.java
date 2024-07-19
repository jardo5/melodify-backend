package com.melodify.Melodify.Config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testPermitAllEndpoints() throws Exception {
        mockMvc.perform(get("/users/signup")).andExpect(status().isOk());
        mockMvc.perform(get("/users/login")).andExpect(status().isOk());
        mockMvc.perform(get("/songs/top")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
    }

    @Test
    public void testAuthenticatedEndpoints() throws Exception {
        mockMvc.perform(get("/songs/search?query=DNA.")).andExpect(status().isUnauthorized());
    }
}
