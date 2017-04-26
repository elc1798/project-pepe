package tech.elc1798.projectpepe.net;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tech.elc1798.projectpepe.Constants;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class NetworkRequestAsyncTaskTest {

    Context testContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void useAppContext() throws Exception {
        assertEquals("tech.elc1798.projectpepe", testContext.getPackageName());
    }

    @Test
    public void serverStatusTest() throws Exception {
        CountDownLatch waiter = new CountDownLatch(1);

        new NetworkRequestAsyncTask(new NetworkTestCallback(waiter, "pepe is receiving memes! feels good man :')"))
                .execute(Constants.PEPE_STATUS_URL);

        waiter.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void imageListRetrievalTest() throws Exception {
        CountDownLatch waiter = new CountDownLatch(1);

        new NetworkRequestAsyncTask(
                new NetworkTestCallback(waiter, "static/test/programmerhumor.png, static/test/feelsgood.png")
        ).execute(Constants.PEPE_TEST_URL);

        waiter.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void fileUploadTest() throws Exception {
        // Create a test file
        File testDirectory = testContext.getDir("tests", Context.MODE_PRIVATE);
        File testFile = new File(testDirectory, "test.txt");

        // Generate a random message and write it to the file
        String randomMessage = UUID.randomUUID().toString();
        FileOutputStream outputStream = new FileOutputStream(testFile);
        outputStream.write(randomMessage.getBytes());
        outputStream.close();

        // Build a countdownlatch to wait for the async task to finish
        CountDownLatch waiter = new CountDownLatch(1);

        // Call the async task with a callback
        new NetworkRequestAsyncTask(new NetworkTestCallback(waiter, randomMessage), testFile).execute(
                Constants.PEPE_FILE_UPLOAD_TEST_URL
        );

        // Wait for latch to countdown or for time to expire
        waiter.await(10, TimeUnit.SECONDS);

        // Remove directory on exit
        testDirectory.deleteOnExit();
    }

    private class NetworkTestCallback extends NetworkOperationCallback {

        private String correctResp;
        private CountDownLatch waiter;

        NetworkTestCallback(CountDownLatch waiter, String correctResp) {
            this.waiter = waiter;
            this.correctResp = correctResp;
        }

        @Override
        public void parseNetworkOperationContents(String contents) {
            if (contents == null) {
                assertFalse(true); // Force crash
            }

            Log.d("NetworkTest:", contents);
            assertEquals(correctResp, contents);

            this.waiter.countDown();
        }
    }
}