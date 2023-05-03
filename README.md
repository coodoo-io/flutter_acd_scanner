# acd_scanner

This is a plugin for the [ACD Android Scanner}(https://www.acd-gruppe.de/mobile-geraete/mobile-handheld-computer/m270se/).

With this plugin you are able to connect to your ACD Scanner and get the 
scanned barcode informations from the scanner to your flutter app.

You also can send commands to the scanner.

## Getting Started

Import the Method Channel to send and receive data.
The Channel itself is a singleton so you will only have on instance per device.

```
MethodChannelAcdScanner().listenOnData.listen((event) {
        log(event);
        data = event;
        setState(() {});
      });
MethodChannelAcdScanner()
    .sendMessage(what: MethodChannelAcdScanner.WHAT_SCAN);
```

You can send various predefined events

```
  @override
  Future<String> activateScan() async {
    return sendMessage(what: WHAT_SCAN, arg1: 1);
  }

  @override
  Future<String> deactiveScane() async {
    return sendMessage(what: WHAT_SCAN, arg1: 0);
  }

  @override
  Future<String> enableScanKey() async {
    return sendMessage(what: WHAT_KEY_ENABLE, arg1: 1);
  }

  @override
  Future<String> disableScanKey() async {
    return sendMessage(what: WHAT_KEY_ENABLE, arg1: 0);
  }

  @override
  Future<String> enableScanBeam() async {
    return sendMessage(what: WHAT_BEAM, arg1: 1);
  }

  @override
  Future<String> disableScanBeam() async {
    return sendMessage(what: WHAT_BEAM, arg1: 0);
  }
```

You also can send your own custom message

```
@override
  Future<String> sendMessage({required int what, int? arg1, int? arg2}) async {
    Map<String, int> params = {'what': what, 'arg1': 0, 'arg2': 0};
    return await methodChannel.invokeMethod('sendMessage', params);
  }
```

There is also an example app which you can directly build to you handscanner and test.

If you have any problems with the lib just feel free to open an issue.

Happy Coding with Coodoo




