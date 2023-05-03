// ignore_for_file: constant_identifier_names

import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'acd_scanner_platform_interface.dart';

class MethodChannelAcdScanner extends AcdScannerPlatform {
  static final MethodChannelAcdScanner _instance =
      MethodChannelAcdScanner._internal();
  factory MethodChannelAcdScanner() => _instance;

  MethodChannelAcdScanner._internal() {
    // init things inside this
    _stream.receiveBroadcastStream().listen((onData) {
      _controller.add(onData);
    });
  }

  static const int WHAT_OK = 1;
  static const int WHAT_HELLO = 2;
  static const int WHAT_SCAN = 4;
  static const int WHAT_SET_TARGET = 5;
  static const int WHAT_BARCODE = 6;
  static const int WHAT_KEY_ENABLE = 7;
  static const int WHAT_BEAM = 8;

  /// The method channel to send data to the scanner
  @visibleForTesting
  final methodChannel = const MethodChannel('acd_scanner');

  /// The event channel to receive data from the scanner
  final EventChannel _stream = const EventChannel('barcode_scan');

  final StreamController<String> _controller = StreamController.broadcast();

  Stream get listenOnData {
    return _controller.stream;
  }

  @override
  Future<String> sendMessage({required int what, int? arg1, int? arg2}) async {
    Map<String, int> params = {'what': what, 'arg1': 0, 'arg2': 0};
    return await methodChannel.invokeMethod('sendMessage', params);
  }

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
}
