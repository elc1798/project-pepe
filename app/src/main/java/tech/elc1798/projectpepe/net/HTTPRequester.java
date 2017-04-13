package tech.elc1798.projectpepe.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The {@code HTTPRequester} class abstracts and handles making GET requests to the MovieDB API.
 */
public class HTTPRequester {

    private static final String TAG = "PEPE/HTTPREQUEST::";
    private static final String GET_REQUESTER_ERROR_LOG_TAG = "GET_REQUESTER_ERROR";
    private static final String BAD_INTERNET_CONNECTION_ERROR = "Internet connection failed. Check your internet.";
    private static final String BAD_SELECTED_FILE_FOR_UPLOAD_ERROR = "Bad file was attempted to be uploaded";
    private static final String GET_PROTOCOL_IDENTIFIER = "GET";
    private static final String POST_PROTOCOL_IDENTIFIER = "POST";

    // HTTP Form POST constants
    private static final String CRLF = "\r\n";
    private static final String HTTP_FORM_DELIMITER = "--";
    private static final String HTTP_FORM_BOUNDARY = "*****";
    private static final String FILE_UPLOAD_PARAMETER_KEY = "file";
    private static final String HTTP_CONTENT_DISP = "Content-Disposition: form-data; name=\"file\";filename=\"%s\"";
    private static final String BAD_UPLOAD_RESP = "bad";
    private static final int HTTP_UPLOAD_MAX_BUFFER_SIZE = 1024 * 1024;

    private static final String[][] HTTP_FILE_UPLOAD_REQUEST_PROPERTIES = {
            {"Connection", "Keep-Alive"},
            {"ENCTYPE", "multipart/form-data"},
            {"Content-Type", "multipart/form-data;boundary=" + HTTP_FORM_BOUNDARY}
    };

    /**
     * Makes a GET request to the target URL and returns the data the URL provided as a String. The returned string will
     * need to be analyzed to determine its format (HTML, JSON, XML, etc.)
     *
     * @param urlString The target URL
     * @return A string containing the data returned by the GET request
     * @throws Exception
     */
    private static String makeGETRequest(String urlString) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(GET_PROTOCOL_IDENTIFIER);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        reader.close();
        return result.toString();
    }

    /**
     * Sends a POST request to a URL and uploads the provided File object.
     * <p>
     * Courtesy of http://www.coderzheaven.com/2012/04/26/upload-image-android-device-server-method-4/
     *
     * @param urlString String containing URL of fileserver
     * @param file      File object to upload
     * @return File server response contents
     */
    public static String makePOSTFileUploadRequest(String urlString, File file) {
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;

        if (!file.isFile()) {
            Log.e(TAG, BAD_SELECTED_FILE_FOR_UPLOAD_ERROR);
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            URL url = new URL(urlString);

            // Open HTTP connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);                                  // Allow Inputs
            conn.setDoOutput(true);                                 // Allow Outputs
            conn.setUseCaches(false);                               // Don't use a Cached Copy
            conn.setRequestMethod(POST_PROTOCOL_IDENTIFIER);        // Set request to use POST

            // Set Request Properties
            for (String[] property : HTTP_FILE_UPLOAD_REQUEST_PROPERTIES) {
                conn.setRequestProperty(property[0], property[1]);
            }

            // Set the file name
            conn.setRequestProperty(FILE_UPLOAD_PARAMETER_KEY, file.getName());
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(HTTP_FORM_DELIMITER + HTTP_FORM_BOUNDARY + CRLF);
            dos.writeBytes(String.format(HTTP_CONTENT_DISP, file.getName()) + CRLF);
            dos.writeBytes(CRLF);

            bytesAvailable = fileInputStream.available();           // create a buffer of maximum size

            bufferSize = Math.min(bytesAvailable, HTTP_UPLOAD_MAX_BUFFER_SIZE);
            buffer = new byte[bufferSize];

            // Read the file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            // Write the file contents into the uploaded form
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, HTTP_UPLOAD_MAX_BUFFER_SIZE);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // Send multipart form data necessary after file data
            dos.writeBytes(CRLF);
            dos.writeBytes(HTTP_FORM_DELIMITER + HTTP_FORM_BOUNDARY + HTTP_FORM_DELIMITER + CRLF);

            // Responses from the server (code and message)
            String serverResponseMessage = conn.getResponseMessage();

            // Close file streams
            fileInputStream.close();
            dos.flush();
            dos.close();

            Log.d(TAG, serverResponseMessage);
            return serverResponseMessage;
        } catch (Exception e) {
            Log.e(TAG, BAD_INTERNET_CONNECTION_ERROR);
        }
        return BAD_UPLOAD_RESP;
    }

    /**
     * Makes a GET request to a URL safely. If an exception is thrown within, returns null.
     *
     * @param url the URL to make a GET request to
     * @return a String
     */
    public static String makeSafeGETRequest(String url) {
        try {
            return makeGETRequest(url);
        } catch (Exception e) {
            Log.d(GET_REQUESTER_ERROR_LOG_TAG, BAD_INTERNET_CONNECTION_ERROR, e);
            return null;
        }
    }
}
