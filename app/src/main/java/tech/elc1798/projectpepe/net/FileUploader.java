package tech.elc1798.projectpepe.net;


import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import tech.elc1798.projectpepe.Constants;

import static tech.elc1798.projectpepe.Constants.FILE_UPLOAD_FAIL_MESSAGE;
import static tech.elc1798.projectpepe.Constants.FILE_UPLOAD_SUCCESS_MESSAGE;

public class FileUploader {

    public static void uploadFile(final Context context, File file, String url, final CountDownLatch countDownLatch) {
        new NetworkRequestAsyncTask(new NetworkOperationCallback() {
            @Override
            public void parseNetworkOperationContents(String contents) {
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }

                if (contents.equals(Constants.PEPE_FILE_UPLOAD_SUCCESS_RESP)) {
                    Toast.makeText(context, FILE_UPLOAD_SUCCESS_MESSAGE, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(context, FILE_UPLOAD_FAIL_MESSAGE, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }, file).execute(url);
    }

}
