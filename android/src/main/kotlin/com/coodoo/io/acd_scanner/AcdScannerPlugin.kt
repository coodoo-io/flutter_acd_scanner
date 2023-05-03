package com.coodoo.io.acd_scanner

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.BinaryMessenger

import android.os.Messenger
import android.content.Context
import android.app.Activity
import android.util.Log
import android.app.Service
import android.content.Intent
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import android.os.Message
import android.os.RemoteException
import android.os.Handler
/** AcdScannerPlugin */
class AcdScannerPlugin: FlutterPlugin, MethodCallHandler {

  var _serviceMessenger: Messenger? = null
  var _eventSink: EventChannel.EventSink? = null
  var _boundToService: Boolean = false
    

  private val WHAT_OK: Int = 1
  private val WHAT_HELLO: Int = 2
  private val WHAT_SCAN: Int = 4
  private val WHAT_SET_TARGET: Int = 5
  private val WHAT_BARCODE: Int = 6
  private val WHAT_KEY_ENABLE: Int = 7
  private val TARGET_MESSAGE: Int = 3

  private var data: String = ""


  private lateinit var eventChannel: EventChannel
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {  
    startListening(flutterPluginBinding.binaryMessenger)
    Log.d("ACD_PLUGIN","Before Channel Registration")
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "acd_scanner")
    channel.setMethodCallHandler(this)
    Log.d("ACD_PLUGIN","Before BindToService")
    bindToService(flutterPluginBinding.applicationContext)
    Log.d("ACD_PLUGIN","Before BindToService")
  }

  fun bindToService(context: Context) {
    try {
      Log.d("ACD_PLUGIN","Intent BEGIN")
      val intent: Intent = Intent()
      intent.setComponent(ComponentName("de.acdgruppe.scanservice", "de.acdgruppe.scanservice.AcdScannerService"))
      // for Longrange use: new ComponentName("de.acdgruppe.scanservicelr", "de.acdgruppe.scanservicelr.AcdScannerServiceLR")
      Log.d("ACD_PLUGIN","Intent STARTED")
      try {
          // Use applicationContext because the activity can change during rotation and it is not
          // obvious what happens to the service if bound to the activity.
          val bindService = context.bindService(intent, _connection, Context.BIND_AUTO_CREATE)
          Log.d("ACD_PLUGIN","${bindService}")
          if (!bindService) {
              Log.d("ACD_PLUGIN","CONNECTION_NOT_BINDED")
              data += "CONNECTION_NOT_ESTABLISHED"
              return
          }

          data += "CONNECTION_ESTABLISHED"
      } catch (e: Exception) {
        Log.d("ACD_PLUGIN","EXCEPTION")
        Log.d("EXCEPTION","${e.printStackTrace()}")
        data += " ${e.toString()}"
      }
    }catch (e: Exception) {
      Log.d("ACD_PLUGIN","EXCEPTION")
      _eventSink?.success(e.toString())
      data += " ${e.toString()}"
    }
  }
  
  
    private val _connection: ServiceConnection = object: ServiceConnection {

      override fun onServiceConnected(className: ComponentName, service: IBinder) {
          // This is called when the connection with the service has been established, giving us the
          // service object we can use to interact with the service.
          _serviceMessenger = Messenger(service)

          // Send a hello to the service. Next steps follow when the response arrives.
          _boundToService = true
          sendMessage(WHAT_HELLO, 0, 0)
      }

      override fun onServiceDisconnected(className: ComponentName) {
          // This is called when the connection with the service has been
          // unexpectedly disconnected, its process crashed.
          _serviceMessenger = null
          _boundToService = false
      }
  }

  fun sendMessage(what: Int, arg1: Int, arg2: Int) {
      if (_serviceMessenger == null) {
          // The messenger should never be null, there's one not-changing instance during the app's life.
          Log.d("MESSANGER", "MESSANGER SHOULD NEVER BE NULL")
          return
      }

      if (!_boundToService) {
          return
      }

      try {
          val msg: Message = Message.obtain(null, what, arg1, arg2)
          msg.replyTo = _receiverMessenger
          _serviceMessenger?.send(msg)
      } catch (e: RemoteException) {
        Log.d("EXCEPTION","${e.printStackTrace()}")
      }
  }

  inner class IncomingHandler : Handler() {
      override fun handleMessage (message: Message) {
        Log.d("ACD_PLUGIN","RECEIVE MESSAGE FROM HANDLER")
        Log.d("ACD_PLUGIN","${message.what}")
        when (message.what) {
            WHAT_OK -> {
                // arg1 contains the reason for this OK message
                when (message.arg1) {

                    WHAT_HELLO -> {
                        // We are connected => set the service to send barcode messages via messenger
                        sendMessage(WHAT_SET_TARGET, TARGET_MESSAGE, 0)
                        // Enable the scankey trigger
                        sendMessage(WHAT_KEY_ENABLE, 1, 0)
                    }
                }
              }
            WHAT_BARCODE -> {
                ///HERE SEND TO DART
                if (_eventSink != null) {
                  _eventSink?.success(message.getData().getString("barcode"))
                }
                Log.d("SENDER", "${message.getData().getString("barcode")}")
            }
        }
    }
  }

  val _receiverMessenger: Messenger = Messenger(IncomingHandler())

  fun startListening(messenger: BinaryMessenger?) {
    eventChannel = EventChannel(messenger, "barcode_scan")
    eventChannel.setStreamHandler(
       object : EventChannel.StreamHandler {
              
              override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink) {
                Log.d("EVENTCHANNEL","onListen")
                _eventSink = eventSink
                _eventSink?.success(data)
              }

              override fun onCancel(p0: Any?) {
                Log.d("EVENTCHANNEL", "OnCancel")
              }
          }
      )
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "sendMessage") {
      Log.d("MESSAGE RECEIVED", "${requireNotNull(call.argument<Int>("what"))}")
      Log.d("MESSAGE RECEIVED", "${requireNotNull(call.argument<Int>("arg1"))}")
      Log.d("MESSAGE RECEIVED", "${requireNotNull(call.argument<Int>("arg2"))}")
      val what = requireNotNull(call.argument<Int>("what"))
      val arg1 = requireNotNull(call.argument<Int>("arg1"))
      val arg2 = requireNotNull(call.argument<Int>("arg2"))
      sendMessage(what, arg1, arg2)
      result.success("OK")
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
