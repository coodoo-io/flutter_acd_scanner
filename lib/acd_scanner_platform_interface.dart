import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'acd_scanner_method_channel.dart';

abstract class AcdScannerPlatform extends PlatformInterface {
  /// Constructs a AcdScannerPlatform.
  AcdScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static AcdScannerPlatform _instance = MethodChannelAcdScanner();

  /// The default instance of [AcdScannerPlatform] to use.
  ///
  /// Defaults to [MethodChannelAcdScanner].
  static AcdScannerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AcdScannerPlatform] when
  /// they register themselves.
  static set instance(AcdScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String> sendMessage({required int what, int? arg1, int? arg2}) {
    throw Exception('Not implemented yet');
  }

  Future<String> activateScan() async {
    throw Exception('Not implemented yet');
  }

  Future<String> deactiveScane() async {
    throw Exception('Not implemented yet');
  }

  Future<String> enableScanKey() async {
    throw Exception('Not implemented yet');
  }

  Future<String> disableScanKey() async {
    throw Exception('Not implemented yet');
  }

  Future<String> enableScanBeam() async {
    throw Exception('Not implemented yet');
  }

  Future<String> disableScanBeam() async {
    throw Exception('Not implemented yet');
  }
}
