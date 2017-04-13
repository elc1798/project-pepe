package tech.elc1798.projectpepe.net;

import android.os.AsyncTask;

import java.io.File;

public class NetworkRequestAsyncTask extends AsyncTask<String, Void, String[]> {

    public enum RequestProtocols {
        GET, POST
    }

    private RequestProtocols protocol;
    private File file;

    public NetworkRequestAsyncTask() {
        this.protocol = RequestProtocols.GET;
        this.file = null;
    }

    public NetworkRequestAsyncTask(File file) {
        this.protocol = RequestProtocols.POST;
        this.file = file;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String[] doInBackground(String... params) {
        String[] responses = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            String url = params[i];

            if (this.protocol == RequestProtocols.GET) {
                responses[i] = HTTPRequester.makeSafeGETRequest(url);
            } else if (this.protocol == RequestProtocols.POST) {
                responses[i] = HTTPRequester.makePOSTFileUploadRequest(url, file);
            }
        }
        return responses;
    }
}
