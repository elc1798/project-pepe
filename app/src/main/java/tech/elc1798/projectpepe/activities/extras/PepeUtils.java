package tech.elc1798.projectpepe.activities.extras;

import tech.elc1798.projectpepe.Constants;

/**
 * Utility class containing methods used to get important values required to interface with the Project PEPE server.
 *
 * Note that a URL is a full link (https://google.com/blah1/blah2/blah3), whereas the route is the path extending from
 * the root of the URL (/blah1/blah2/blah3 from the example above). The gallery ID is the identifier for the gallery
 * used by the image.
 */
public class PepeUtils {

    /**
     * Gets the URL of an image given its ID
     *
     * @param imageID the id of the image we want the URL for
     * @return a String representing the URL of the image
     */
    public static String getImageURL(String imageID) {
        return Constants.PEPE_ROOT_URL + imageID;
    }

    /**
     * Gets the gallery URL from the image ID
     *
     * @param imageID The image ID to extract the gallery URL from
     * @return a String representing the URL to the gallery
     */
    public static String getGalleryRouteFromImageID(String imageID) {
        // The gallery route is simply the Image ID without the .png extension
        return imageID.substring(0, imageID.lastIndexOf('.'));
    }

    /**
     * Gets the route for a gallery image
     *
     * @param galleryRoute The gallery's ID
     * @param imageID      The image ID to retrieve
     * @return a String representing a route
     */
    public static String getGalleryImageRoute(String galleryRoute, int imageID) {
        return galleryRoute + Constants.URL_PATH_SEPARATOR + imageID +
                Constants.PEPE_IMAGE_EXTENSION;
    }

    /**
     * Gets the gallery ID from its route
     *
     * @param galleryRoute The gallery route
     * @return String containing the gallery ID
     */
    public static String getGalleryIDFromRoute(String galleryRoute) {
        return galleryRoute.substring(galleryRoute.lastIndexOf(Constants.URL_PATH_SEPARATOR) + 1);
    }
}
