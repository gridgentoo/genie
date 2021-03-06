/*
 *
 *  Copyright 2016 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.controllers;

import com.netflix.genie.test.categories.UnitTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * Unit tests for UIController class.
 *
 * @author tgianos
 * @since 3.0.0
 */
@Category(UnitTest.class)
public class UIControllerUnitTests {

    private UIController controller;

    /**
     * Setup for the tests.
     */
    @Before
    public void setup() {
        this.controller = new UIController();
    }

    /**
     * Make sure the getIndex endpoint returns the right template name.
     */
    @Test
    public void canGetIndex() {
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        final ArgumentCaptor<Cookie> cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        Assert.assertThat(this.controller.getIndex(response, null), Matchers.is("index"));
        Mockito.verify(response, Mockito.times(1)).addCookie(cookieArgumentCaptor.capture());
        Assert.assertThat(cookieArgumentCaptor.getValue().getName(), Matchers.is("genie.user"));
        Assert.assertThat(cookieArgumentCaptor.getValue().getValue(), Matchers.is("Genie User"));

        final Authentication authentication = Mockito.mock(Authentication.class);
        final String name = UUID.randomUUID().toString();
        Mockito.when(authentication.getName()).thenReturn(name);
        Assert.assertThat(this.controller.getIndex(response, authentication), Matchers.is("index"));
        Mockito.verify(response, Mockito.times(2)).addCookie(cookieArgumentCaptor.capture());
        Assert.assertThat(cookieArgumentCaptor.getValue().getName(), Matchers.is("genie.user"));
        Assert.assertThat(cookieArgumentCaptor.getValue().getValue(), Matchers.is(name));
    }

    /**
     * Make sure the getFile method returns the right forward command.
     * @throws Exception if an error occurs
     */
    @Test
    public void canGetFile() throws Exception {
        final String id = UUID.randomUUID().toString();
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito
            .when(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
            .thenReturn("/file/" + id + "/output/genie/log.out");
        Mockito
            .when(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
            .thenReturn("/file/{id}/**");

        final String encodedId = URLEncoder.encode(id, "UTF-8");
        final String expectedPath = "/api/v3/jobs/" + encodedId + "/output/genie/log.out";

        Assert.assertThat(
            this.controller.getFile(id, request),
            Matchers.is("forward:" + expectedPath)
        );
    }
}
