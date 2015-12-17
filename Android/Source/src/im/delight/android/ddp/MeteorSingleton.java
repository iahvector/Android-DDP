package im.delight.android.ddp;

/**
 * Copyright 2014 www.delight.im <info@delight.im>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;
import java.util.LinkedList;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.Meteor;

/** Provides a single access point to the `Meteor` class that can be used across `Activity` instances */
public class MeteorSingleton extends Meteor implements MeteorCallback {

	private static final String TAG = "MeteorSingleton";
	private static MeteorSingleton mInstance;
	private EventHandler handler;

	public synchronized static MeteorSingleton createInstance(final Context context, final String serverUri) {
		return createInstance(context, serverUri, null);
	}

	public synchronized static MeteorSingleton createInstance(final Context context, final String serverUri, final String protocolVersion) {
		if (mInstance != null) {
			throw new RuntimeException("An instance has already been created");
		}

		if (protocolVersion == null) {
			mInstance = new MeteorSingleton(context, serverUri);
		}
		else {
			mInstance = new MeteorSingleton(context, serverUri, protocolVersion);
		}

		mInstance.setCallback(mInstance);

		return mInstance;
	}

	public synchronized static MeteorSingleton getInstance() {
		if (mInstance == null) {
			throw new RuntimeException("Please call `createInstance(...)` first");
		}

		return mInstance;
	}

	public synchronized static boolean hasInstance() {
		return mInstance != null;
	}

	@Override
	public void setCallback(final MeteorCallback callback) {
		if (handler != null && callback != null) {
			handler.setCallback(callback);
		}
	}

	public void unsetCallback(final MeteorCallback callback) {
		if (handler != null && callback != null) {
			handler.unsetCallback(callback);
		}
	}

	private MeteorSingleton(final Context context, final String serverUri) {
		super(context, serverUri);
	}

	private MeteorSingleton(final Context context, final String serverUri, final String protocolVersion) {
		super(context, serverUri, protocolVersion);

		handler = new EventHandler(Looper.getMainLooper());
	}

	@Override
	public void onConnect(final boolean signedInAutomatically) {
		log(TAG);
		log("  onConnect");
		log("    signedInAutomatically == "+signedInAutomatically);

		if (handler != null) {
			Message msg = Message.obtain();
			msg.what = EventHandler.EVENT_CONNECTION_OPENED;
			msg.arg1 = signedInAutomatically? 1 : 0;
			handler.sendMessage(msg);
		}
	}

	@Override
	public void onDisconnect() {
		log(TAG);
		log("  onDisconnect");

		if (handler != null) {
			handler.sendEmptyMessage(EventHandler.EVENT_CONNECTION_CLOSED);
		}
	}

	@Override
	public void onDataAdded(final String collectionName, final String documentID, final String newValuesJson) {
		log(TAG);
		log("  onDataAdded");
		log("    collectionName == "+collectionName);
		log("    documentID == "+documentID);
		log("    newValuesJson == "+newValuesJson);

		if (handler != null) {
			Bundle b = new Bundle();
			b.putString(EventHandler.PARAM_COLLECTION_NAME, collectionName);
			b.putString(EventHandler.PARAM_DOCUMENT_ID, documentID);
			b.putString(EventHandler.PARAM_NEW_VALUES_JSON, newValuesJson);

			Message msg = Message.obtain();
			msg.what = EventHandler.EVENT_DATA_ADDED;
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}

	@Override
	public void onDataChanged(final String collectionName, final String documentID, final String updatedValuesJson, final String removedValuesJson) {
		log(TAG);
		log("  onDataChanged");
		log("    collectionName == "+collectionName);
		log("    documentID == "+documentID);
		log("    updatedValuesJson == "+updatedValuesJson);
		log("    removedValuesJson == "+removedValuesJson);

		if (handler != null) {
			Bundle b = new Bundle();
			b.putString(EventHandler.PARAM_COLLECTION_NAME, collectionName);
			b.putString(EventHandler.PARAM_DOCUMENT_ID, documentID);
			b.putString(EventHandler.PARAM_NEW_VALUES_JSON, updatedValuesJson);
			b.putString(EventHandler.PARAM_REMOVED_VALUES_JSON, removedValuesJson);

			Message msg = Message.obtain();
			msg.what = EventHandler.EVENT_DATA_CHANGED;
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}

	@Override
	public void onDataRemoved(final String collectionName, final String documentID) {
		log(TAG);
		log("  onDataRemoved");
		log("    collectionName == "+collectionName);
		log("    documentID == "+documentID);

		if (handler != null) {
			Bundle b = new Bundle();
			b.putString(EventHandler.PARAM_COLLECTION_NAME, collectionName);
			b.putString(EventHandler.PARAM_DOCUMENT_ID, documentID);

			Message msg = Message.obtain();
			msg.what = EventHandler.EVENT_DATA_REMOVED;
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}

	@Override
	public void onException(final Exception e) {
		log(TAG);
		log("  onException");
		log("    e == "+e);

		if (handler != null) {
			Message msg = Message.obtain();
			msg.what = EventHandler.EVENT_ERROR;
			msg.obj = e;
			handler.sendMessage(msg);
		}
	}

}
