package tech.elc1798.projectpepe;

/**
 * Class that holds constants for the project
 */
public class Constants {

    public static final String PEPE_ROOT_URL = "https://project-pepe-imgs.herokuapp.com/";
    public static final String PEPE_ROOT_GET_PARAMETERS = "?offset=%d";
    public static final String PEPE_IMAGELIST_SEPARATOR = ", ";
    public static final String PEPE_FILE_UPLOAD_URL = "https://project-pepe-imgs.herokuapp.com/upload/";
    public static final String PEPE_FILE_UPLOAD_SUCCESS_RESP = "ok";
    public static final String PEPE_FILE_UPLOAD_TEST_URL = "https://project-pepe-imgs.herokuapp.com/ephemeralupload/";
    public static final String PEPE_STATUS_URL = "https://project-pepe-imgs.herokuapp.com/status/";
    public static final String PEPE_TEST_URL = "https://project-pepe-imgs.herokuapp.com/test/";
    public static final String PEPE_GALLERY_SIZE_URL = "https://project-pepe-imgs.herokuapp.com/gallerycount/";
    public static final String PEPE_GALLERY_ID_GET_PARAMETER = "?gallery_id=%s";
    public static final String PEPE_IMAGE_EXTENSION = ".png";

    public static final String URL_PATH_SEPARATOR = "/";
    public static final String IMG_CACHE_STORAGE_DIRECTORY = "snapshots";
    public static final String IMG_CACHE_FILENAME_FORMAT = "%s.png";
    public static final String FILE_UPLOAD_SUCCESS_MESSAGE = "Image successfully uploaded!";
    public static final String FILE_UPLOAD_FAIL_MESSAGE = "Server could not save uploaded image!";
    public static final int COMPRESSION_RATE = 100;

}
