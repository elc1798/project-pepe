package tech.elc1798.projectpepe.net;

/**
 * Abstract class for holding callback methods for NetworkRequestAsyncTask. This is specifically an abstract class
 * rather than an interface so we can create anonymous objects.
 */
public abstract class NetworkOperationCallback {

    public abstract void parseNetworkOperationContents(String contents);

}
