package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CardService cardService;
    @MockBean UserService userService;

    @Test
    void transfer_unauthorized_returns_401() throws Exception {
        TransferRequest req = new TransferRequest();
        req.setFromCardId(1L);
        req.setToCardId(2L);
        req.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/cards/transfer")
                        .with(csrf()) // иначе Spring вернёт 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user_0", roles = "USER")
    void transfer_success_returns_200() throws Exception {
        // userService.getByUsername(...) должен вернуть пользователя
        User u = new User();
        u.setId(1L);
        u.setUsername("user_0");

        when(userService.getByUsername("user_0")).thenReturn(u);

        TransferRequest req = new TransferRequest();
        req.setFromCardId(1L);
        req.setToCardId(2L);
        req.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/cards/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(cardService, times(1)).transferBetweenOwnCards(1L, 2L, new BigDecimal("100.00"), u);
    }
}
