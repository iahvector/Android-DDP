package im.delight.android.ddp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by islamhassan on 12/17/15.
 */
public class EventHandler extends Handler {
    private static final String TAG = "Meteor";

    public static final int EVENT_CONNECTION_OPENED = 10;
    public static final int EVENT_CONNECTION_CLOSED = 20;
    public static final int EVENT_DATA_ADDED = 30;
    public static final int EVENT_DATA_CHANGED = 31;
    public static final int EVENT_DATA_REMOVED = 32;
    public static final int EVENT_ERROR = 40;
    public static final int EVENT_LOG_MESSAGE = 50;

    public static final String PARAM_COLLECTION_NAME = "collection-name";
    public static final String PARAM_DOCUMENT_ID = "document-id";
    public static final String PARAM_NEW_VALUES_JSON = "new-values-json";
    public static final String PARAM_REMOVED_VALUES_JSON = "removed-values-json";

    private final List<MeteorCallback> mCallbacks = new LinkedList<MeteorCallback>();

    EventHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        Log.d(TAG, "Message: " + msg.what);
        switch (msg.what) {
            case EVENT_CONNECTION_OPENED: {
                for (MeteorCallback callback : mCallbacks) {
                    if (callback != null) {
                        callback.onConnect(msg.arg1 == 1);
                    }
                }

                break;
            }
            case EVENT_CONNECTION_CLOSED: {
                for (MeteorCallback callback : mCallbacks) {
                    if (callback != null) {
                        callback.onDisconnect();
                    }
                }

                break;
            }
            case EVENT_DATA_ADDED: {
                Bundle b = msg.getData();
                if (b != null) {
                    String collectionName = b.getString(PARAM_COLLECTION_NAME);
                    String documentId = b.getString(PARAM_DOCUMENT_ID);
                    String newJson = b.getString(PARAM_NEW_VALUES_JSON);

                    for (MeteorCallback callback : mCallbacks) {
                        if (callback != null) {
                            callback.onDataAdded(collectionName, documentId, newJson);
                        }
                    }
                }

                break;
            }
            case EVENT_DATA_CHANGED: {
                Bundle b = msg.getData();
                if (b != null) {
                    String collectionName = b.getString(PARAM_COLLECTION_NAME);
                    String documentId = b.getString(PARAM_DOCUMENT_ID);
                    String updatedJson = b.getString(PARAM_NEW_VALUES_JSON);
                    String removedJson = b.getString(PARAM_REMOVED_VALUES_JSON);

                    for (MeteorCallback callback : mCallbacks) {
                        if (callback != null) {
                            callback.onDataChanged(collectionName, documentId, updatedJson, removedJson);
                        }
                    }
                }

                break;
            }
            case EVENT_DATA_REMOVED: {
                Bundle b = msg.getData();
                if (b != null) {
                    String collectionName = b.getString(PARAM_COLLECTION_NAME);
                    String documentId = b.getString(PARAM_DOCUMENT_ID);

                    for (MeteorCallback callback : mCallbacks) {
                        if (callback != null) {
                            callback.onDataRemoved(collectionName, documentId);
                        }
                    }
                }

                break;
            }
            case EVENT_ERROR: {
                Exception e = (Exception) msg.obj;
                Log.d(TAG, "WSS Exception: " + e.getMessage());

                for (MeteorCallback callback : mCallbacks) {
                    if (callback != null) {
                        callback.onException(e);
                    }
                }

                break;
            }
            case EVENT_LOG_MESSAGE: {
                String log = (String) msg.obj;
                Log.d(TAG, log);
                break;
            }
            default: {
                throw new RuntimeException("Unknown event: " + msg.what);
            }
        }
    }

    public void setCallback(MeteorCallback callback) {
        if (callback != null) {
            this.mCallbacks.add(callback);
        }
    }

    public void unsetCallback(MeteorCallback callback) {
        if (callback != null) {
            mCallbacks.remove(callback);
        }
    }
}