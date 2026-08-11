package com.btxtech.server.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The pixel is fetched by an <code>img</code> tag and reported on by a beacon, so the same URL is
 * asked for with both methods. Mapping only one of them cost a warning per landing page view and
 * left the image itself unanswered.
 */
class TrackingPixelControllerTest {
    private final MockMvc mockMvc;

    TrackingPixelControllerTest() throws IOException {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrackingPixelController()).build();
    }

    @Test
    void getServesTheImage() throws Exception {
        var response = mockMvc.perform(get("/t.gif").param("rdt_cid", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_GIF))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse();

        var body = response.getContentAsByteArray();
        assertTrue(body.length > 0, "pixel is empty");
        assertTrue(new String(body, 0, 6, StandardCharsets.US_ASCII).startsWith("GIF"), "not a GIF");
    }

    @Test
    void postAcceptsTheBeacon() throws Exception {
        mockMvc.perform(post("/t.gif").param("e", "exit"))
                .andExpect(status().isNoContent());
    }
}
