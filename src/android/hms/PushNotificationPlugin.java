package hms;

import static hms.HuaweiPushReceiver.ACTION_MESSAGE;
import static hms.HuaweiPushReceiver.ACTION_TOKEN;
import static hms.HuaweiPushReceiver.ACTION_EVENT;
import static hms.HuaweiPushReceiver.ACTION_ERROR;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.push.handler.DeleteTokenHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class PushNotificationPlugin extends CordovaPlugin implements HuaweiPushReceiver.IPushCallback {

	public static String TAG = "hms.PushNotificationPlugin";
	public static String token = "";
	public static int openNotificationId = 0;
	public static String openNotificationExtras;

	private static PushNotificationPlugin instance;
	private static Activity activity;
	private CallbackContext initCallback;

	public PushNotificationPlugin() {
		instance = this;
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		activity = cordova.getActivity();
		// 如果是首次启动，并且点击的通知消息，则处理消息
		if (openNotificationId != 0) {
			notificationOpened(openNotificationId, openNotificationExtras);
		}
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("init")) {
			this.init(callbackContext);
			return true;
		}
		if (action.equals("stop")) {
			this.deleteToken(callbackContext);
			return true;
		}
		return false;
	}

	private void init(CallbackContext callbackContext) {
		HMSAgent.init(activity);
		this.initCallback = callbackContext;
		registerBroadcast();
		getToken();
	}

	private void success(int statusCode) {
		try {
			JSONObject result = new JSONObject();
			result.put("status", String.valueOf(statusCode));
			this.initCallback.success(result.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			this.initCallback.error("Trying to use JSONObject, but get error");
		}
	}

	/**
	 * 获取token | get push token
	 */
	private void getToken() {
		Log.d(TAG, "get token: begin");
		HMSAgent.Push.getToken(new GetTokenHandler() {
			@Override
			public void onResult(int rst) {
				Log.d(TAG, "get token: end code=" + rst);
				success(rst);
			}
		});
	}

	/**
	 * 删除token | delete push token
	 */
	private void deleteToken(CallbackContext callbackContext) {
		this.initCallback = callbackContext;
		Log.d(TAG, "deleteToken:begin");
		HMSAgent.Push.deleteToken(token, new DeleteTokenHandler() {
			@Override
			public void onResult(int rst) {
				Log.d(TAG, "deleteToken:end code=" + rst);
				success(rst);
			}
		});
	}

	/**
	 * Register the onReceive callback
	 */
	private void registerBroadcast() {
		HuaweiPushReceiver.registerPushCallback(this);
	}

	@Override
	public void onReceive(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			if (b != null) {
				if (ACTION_TOKEN.equals(action)) {
					token = b.getString(ACTION_TOKEN);
					onTokenRegistered(token);
				} else if (ACTION_MESSAGE.equals(action)) {
					String payload = b.getString("payload");
					pushMsgReceived(payload);
				} else if (ACTION_EVENT.equals(action)) {
					String payload = b.getString("payload");
					int notifyId = b.getInt("notifyId");
					notificationOpened(notifyId, payload);
				} else if (ACTION_ERROR.equals(action)) {
					String payload = b.getString("payload");
					errorFound(payload);
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		HuaweiPushReceiver.unRegisterPushCallback(this);
	}

	private static void onTokenRegistered(String deviceToken) {
		Log.i(TAG, "-------------onTokenRegistered------------------" + deviceToken);
		if (instance == null) {
			return;
		}
		try {
			JSONObject object = new JSONObject();
			object.put("token", deviceToken);
			String format = "HuaweiPushNotification.tokenRegistered(%s);";
			final String js = String.format(format, object.toString());
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					instance.webView.loadUrl("javascript:" + js);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void pushMsgReceived(String msg) {
		Log.i(TAG, "-------------pushMsgReceived------------------" + msg);
		if (instance == null) {
			return;
		}
		try {
			JSONObject object = new JSONObject();
			object.put("message", msg);
			String format = "HuaweiPushNotification.pushMsgReceived(%s);";
			final String js = String.format(format, object.toString());
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					instance.webView.loadUrl("javascript:" + js);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void notificationOpened(int notifyId, String msg) {
		PushNotificationPlugin.openNotificationId = notifyId;
		PushNotificationPlugin.openNotificationExtras = msg;
		Log.i(TAG, "-------------notificationOpened------------------" + msg);
		if (instance == null) {
			return;
		}
		try {
			JSONObject object = new JSONObject();
			object.put("notifyId", notifyId);
			object.put("message", msg);
			String format = "HuaweiPushNotification.notificationOpened(%s);";
			final String js = String.format(format, object.toString());
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					instance.webView.loadUrl("javascript:" + js);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PushNotificationPlugin.openNotificationId = 0;
		PushNotificationPlugin.openNotificationExtras = "";
	}

	private static void errorFound(String msg) {
		Log.e(TAG, "-------------errorFound------------------" + msg);
		if (instance == null) {
			return;
		}
		try {
			JSONObject object = new JSONObject();
			object.put("message", msg);
			String format = "HuaweiPushNotification.errorFound(%s);";
			final String js = String.format(format, object.toString());
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					instance.webView.loadUrl("javascript:" + js);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
