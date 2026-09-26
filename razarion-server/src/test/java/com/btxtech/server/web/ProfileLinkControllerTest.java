package com.btxtech.server.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileLinkControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ProfileLinkController()).build();

    @Test
    void aProfileLinkLandsTaggedAsAnOwnPost() throws Exception {
        mockMvc.perform(get("/x"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/?utm_source=social-x&utm_medium=social&utm_campaign=bio"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    /** Instagram's in-app browser appends its click id to the bio link; the visit is joined by it. */
    @Test
    void theClickIdMetaAppendedIsPassedOn() throws Exception {
        mockMvc.perform(get("/ig?fbclid=IwZXh0bgNhZW0"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "/?utm_source=social-ig&utm_medium=social&utm_campaign=bio&fbclid=IwZXh0bgNhZW0"));
    }

    @Test
    void aPostLinkNamesThePost() throws Exception {
        mockMvc.perform(get("/fb/own-20260925180000"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "/?utm_source=social-fb&utm_medium=social&utm_campaign=own-20260925180000"));
    }

    @Test
    void aCraftedCampaignCannotBreakOutOfTheQuery() throws Exception {
        mockMvc.perform(get("/yt/a&utm_source=evil"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        "/?utm_source=social-yt&utm_medium=social&utm_campaign=autm_sourceevil"));
    }
}
