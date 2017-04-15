package tech.elc1798.projectpepe.net;

import android.os.AsyncTask;

import java.io.File;

public class NetworkRequestAsyncTask extends AsyncTask<String, Void, String> {

    public enum RequestProtocols {
        GET, POST
    }

    private RequestProtocols protocol;
    private NetworkOperationCallback callback;
    private File file;

    public NetworkRequestAsyncTask(NetworkOperationCallback callback) {
        this.protocol = RequestProtocols.GET;
        this.callback = callback;
        this.file = null;
    }

    public NetworkRequestAsyncTask(NetworkOperationCallback callback, File file) {
        this.protocol = RequestProtocols.POST;
        this.callback = callback;
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
    protected String doInBackground(String... params) {
        String url = params[0];
        if (this.protocol == RequestProtocols.GET) {
            return HTTPRequester.makeSafeGETRequest(url);
        } else if (this.protocol == RequestProtocols.POST) {
            return HTTPRequester.makePOSTFileUploadRequest(url, file);
        }
        return null;
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param string The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);

        if (this.callback != null) {
            this.callback.parseNetworkOperationContents(string);
        }
    }
}
