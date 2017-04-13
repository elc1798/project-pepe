package tech.elc1798.projectpepe.net;

import org.junit.Test;

import static org.junit.Assert.*;

public class HTTPRequesterTest {

    @Test
    public void getRequestTest() throws Exception {
        assertTrue(HTTPRequester.makeSafeGETRequest("https://google.com") != null);
        assertTrue(HTTPRequester.makeSafeGETRequest("https://project-pepe-imgs.herokuapp.com/") != null);
    }
}