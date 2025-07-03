package com.khoubyari.example.test;

/**
 * Uses JsonPath: http://goo.gl/nwXpb, Hamcrest and MockMVC
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khoubyari.example.Application;
import com.khoubyari.example.api.rest.HotelController;
import com.khoubyari.example.domain.Hotel;
import com.khoubyari.example.service.HotelService; // Added import

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Random;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class HotelControllerTest {

    private static final String RESOURCE_LOCATION_PATTERN = "http://localhost/example/v1/hotels/[0-9]+";

    @InjectMocks
    HotelController controller;

    @Autowired
    WebApplicationContext context;

    @Autowired
    private HotelService hotelService; // Autowire HotelService for test setup

    private MockMvc mvc;

    @Before
    public void initTests() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldHaveEmptyDB() throws Exception {
        mvc.perform(get("/example/v1/hotels")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    public void shouldCreateRetrieveDelete() throws Exception {
        Hotel r1 = mockHotel("shouldCreateRetrieveDelete");
        byte[] r1Json = toJson(r1);

        //CREATE
        MvcResult result = mvc.perform(post("/example/v1/hotels")
                .content(r1Json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern(RESOURCE_LOCATION_PATTERN))
                .andReturn();
        long id = getResourceIdFromUrl(result.getResponse().getRedirectedUrl());

        //RETRIEVE
        mvc.perform(get("/example/v1/hotels/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is(r1.getName())))
                .andExpect(jsonPath("$.city", is(r1.getCity())))
                .andExpect(jsonPath("$.description", is(r1.getDescription())))
                .andExpect(jsonPath("$.rating", is(r1.getRating())));

        //DELETE
        mvc.perform(delete("/example/v1/hotels/" + id))
                .andExpect(status().isNoContent());

        //RETRIEVE should fail
        mvc.perform(get("/example/v1/hotels/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //todo: you can test the 404 error body too.

/*
JSONAssert.assertEquals(
  "{foo: 'bar', baz: 'qux'}",
  JSONObject.fromObject("{foo: 'bar', baz: 'xyzzy'}"));
 */
    }

    @Test
    public void shouldCreateAndUpdateAndDelete() throws Exception {
        Hotel r1 = mockHotel("shouldCreateAndUpdate");
        byte[] r1Json = toJson(r1);
        //CREATE
        MvcResult result = mvc.perform(post("/example/v1/hotels")
                .content(r1Json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern(RESOURCE_LOCATION_PATTERN))
                .andReturn();
        long id = getResourceIdFromUrl(result.getResponse().getRedirectedUrl());

        Hotel r2 = mockHotel("shouldCreateAndUpdate2");
        r2.setId(id);
        byte[] r2Json = toJson(r2);

        //UPDATE
        result = mvc.perform(put("/example/v1/hotels/" + id)
                .content(r2Json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        //RETRIEVE updated
        mvc.perform(get("/example/v1/hotels/" + id)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is(r2.getName())))
                .andExpect(jsonPath("$.city", is(r2.getCity())))
                .andExpect(jsonPath("$.description", is(r2.getDescription())))
                .andExpect(jsonPath("$.rating", is(r2.getRating())));

        //DELETE
        mvc.perform(delete("/example/v1/hotels/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldFailToCreateHotelWithInvalidData() throws Exception {
        Hotel r1 = mockHotel("shouldFailToCreateHotelWithInvalidData");
        r1.setName(null); // Invalid data
        byte[] r1Json = toJson(r1);

        mvc.perform(post("/example/v1/hotels")
                .content(r1Json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cause.message", containsString("DataFormatException: name")));
    }

    @Test
    public void shouldGetAllHotelsWithPagination() throws Exception {
        // Create a few hotels to test pagination
        Hotel h1 = hotelService.createHotel(mockHotel("PageHotel1"));
        Hotel h2 = hotelService.createHotel(mockHotel("PageHotel2"));
        Hotel h3 = hotelService.createHotel(mockHotel("PageHotel3"));

        // Test first page, size 2
        mvc.perform(get("/example/v1/hotels")
                .param("page", "0")
                .param("size", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.totalPages", is(2)));

        // Test second page, size 2
        mvc.perform(get("/example/v1/hotels")
                .param("page", "1")
                .param("size", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1))) // Remaining element
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }


    /*
    ******************************
     */

    private long getResourceIdFromUrl(String locationUrl) {
        String[] parts = locationUrl.split("/");
        return Long.valueOf(parts[parts.length - 1]);
    }


    private Hotel mockHotel(String prefix) {
        Hotel r = new Hotel();
        r.setCity(prefix + "_city");
        r.setDescription(prefix + "_description");
        r.setName(prefix + "_name");
        r.setRating(new Random().nextInt(6));
        return r;
    }

    private byte[] toJson(Object r) throws Exception {
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(r).getBytes();
    }

    // match redirect header URL (aka Location header)
    private static ResultMatcher redirectedUrlPattern(final String expectedUrlPattern) {
        return new ResultMatcher() {
            public void match(MvcResult result) {
                Pattern pattern = Pattern.compile("\\A" + expectedUrlPattern + "\\z");
                assertTrue(pattern.matcher(result.getResponse().getRedirectedUrl()).find());
            }
        };
    }

}
