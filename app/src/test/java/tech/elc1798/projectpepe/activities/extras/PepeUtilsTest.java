package tech.elc1798.projectpepe.activities.extras;

import org.junit.Test;

import static org.junit.Assert.*;

public class PepeUtilsTest {
    private String imageID = "static/uploads/0.png";

    @Test
    public void getImageURL() throws Exception {
        assertEquals(PepeUtils.getImageURL(imageID), "https://project-pepe-imgs.herokuapp.com/static/uploads/0.png");
    }

    @Test
    public void getGalleryRouteFromImageID() throws Exception {
        assertEquals(PepeUtils.getGalleryRouteFromImageID(imageID), "static/uploads/0");
    }

    @Test
    public void getGalleryImageRoute() throws Exception {
        assertEquals(PepeUtils.getGalleryImageRoute("static/uploads/0", 0), "static/uploads/0/0.png");
    }

    @Test
    public void getGalleryIDFromRoute() throws Exception {
        assertEquals(PepeUtils.getGalleryIDFromRoute("static/uploads/0"), "0");
    }
}