import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

class FlutterNitrite {
  static const MethodChannel _channel =
      const MethodChannel('flutter_nitrite');
  
  static const EventChannel subscribeToCollection = const EventChannel("com.lakex.flutternitriteexample/stream");

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> openDatabase(String dbName) async{
    final String db= await _channel.invokeMethod("openDatabase", {"dbName": dbName});
    return db;
  }

  static Future<String> getCollection(String name) async{
    final String result = await _channel.invokeMethod("getCollection", {"name": name});
    return result;
  }

  static Future<Map> createDocument(String collectionName, Map<String, dynamic> doc) async{
    String _doc = json.encode(doc);
    Map result = await _channel.invokeMethod("createDocument", {"collectName": collectionName, "document": _doc});
    return result;
  }

  static Future<List> find(String collectionName, {int offset=0, int limit=15}) async{
    List result = await _channel.invokeMethod("find", {"collectName": collectionName, "offset": offset, "limit": limit});
    return result;
  }

  static Future<Map> findById(String collectionName, int id) async{
    Map result = await _channel.invokeMethod("findById", {"collectName": collectionName, "id": id});
    return result;
  }

  
  static Future<List> findWhere(String collectionName, String field, dynamic value, {int offset=0, int limit=15}) async{
    List result = await _channel.invokeMethod("findWhere", {"collectName": collectionName, "field": field, "value": value, "offset": offset, "limit": limit});
    return result;
  }

  static Future<Map> update(String collectionName,  Map doc, {bool upsert=false}) async{
    String _doc = json.encode(doc);
    Map result = await _channel.invokeMethod("update", {"collectName": collectionName, "upsert": upsert, "document": _doc});
    return result;

  }

  static Future<String> deleteDocument(String collectionName, {int id, bool empty = false}) async{
    String result = await _channel.invokeMethod("deleteDocument", {"collectName": collectionName, "id": id, "empty": empty});
    return result;
  }

  static Future<Map> subscribe(String collectionName) async{
    Map result = await _channel.invokeMethod("subscribe", {"collectName": collectionName});
    return result;
  }

}
