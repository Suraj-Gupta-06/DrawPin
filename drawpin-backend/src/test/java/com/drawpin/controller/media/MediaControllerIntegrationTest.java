package com.drawpin.controller.media;

import com.drawpin.domain.entity.User;
import com.drawpin.domain.enums.UserStatus;
import com.drawpin.repository.UserRepository;
import com.drawpin.service.media.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.drawpin.security.DrawPinUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private FileStorageService fileStorageService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test.media@example.com")
                .passwordHash("password")
                .name("Test User")
                .handle("test.user.media")
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Test
    void uploadMedia_ValidFile_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "image content".getBytes());

        when(fileStorageService.upload(any(), anyString())).thenReturn(Map.of(
                "public_id", "test_id",
                "secure_url", "https://example.com/test.jpg"
        ));

        DrawPinUserDetails userDetails = new DrawPinUserDetails(testUser);

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("mediaType", "AVATAR")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.mediaType", is("AVATAR")));
    }

    @Test
    void uploadMedia_InvalidType_Returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.js", "application/javascript", "alert(1)".getBytes());

        DrawPinUserDetails userDetails = new DrawPinUserDetails(testUser);

        mockMvc.perform(multipart("/api/v1/media/upload")
                        .file(file)
                        .param("mediaType", "AVATAR")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.message", is("Only images and videos are supported")));
    }
}
