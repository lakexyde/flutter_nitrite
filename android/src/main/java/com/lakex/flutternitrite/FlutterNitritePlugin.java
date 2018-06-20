package com.lakex.flutternitrite;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import io.flutter.util.PathUtils;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.Document;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.SortOrder;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.FindOptions;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.mapper.JacksonMapper;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangedItem;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.util.Iterables;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * FlutterNitritePlugin
 */
public class FlutterNitritePlugin implements MethodCallHandler {
  private final Registrar registrar;
  private final MethodChannel channel;
  final EventChannel collectionChannel;
  private Result result;

  private Nitrite db;

  private static final String ERROR_DB = "Nitrite Database Error";
  public static final String STREAM = "com.lakex.flutternitrite/stream";
  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_nitrite");
    final EventChannel collectionChannel = new EventChannel(registrar.messenger(), STREAM);
    channel.setMethodCallHandler(new FlutterNitritePlugin(registrar, channel, collectionChannel));
  }

  private FlutterNitritePlugin(Registrar registrar, MethodChannel channel, EventChannel collectionChannel){
    this.registrar = registrar;
    this.channel = channel;
    this.collectionChannel = collectionChannel;
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("openDatabase")){
      String db = call.argument("dbName");
      openDatabase(db, result);

    } else if(call.method.equals("createDocument")){
      String collectionName = call.argument("collectName");
      String document = call.argument("document");
      createDocument(collectionName, document, result);

    } else if(call.method.equals("find")){
      String collectionName = call.argument("collectName");
      int offset = call.argument("offset");
      int limit = call.argument("limit");
      find(collectionName, offset, limit, result);

    } else if(call.method.equals("findById")){
      String collectionName = call.argument("collectName");
      Long id = call.argument("id");
      findById(collectionName, id, result);
    } else if(call.method.equals("update")){
      String collectionName = call.argument("collectName");
      String document = call.argument("document");
      boolean upsert = (call.argument("upsert")) ? true : false;
      update(collectionName, document, upsert, result);
    } else if(call.method.equals("subscribe")){ 
      String collectionName = call.argument("collectName");
      subscribe(collectionName, result);
    } else if(call.method.equals("findWhere")){
      String collectionName = call.argument("collectName");
      String field = call.argument("field");
      Object value = call.argument("value");
      int offset = call.argument("offset");
      int limit = call.argument("limit");
      findWhere(collectionName, field, value, limit, offset, result);
    } else if(call.method.equals("deleteDocument")){
      String collectionName = call.argument("collectName");
      Long id = call.argument("id");
      boolean empty = call.argument("empty");
      deleteDocument(collectionName, id, empty, result);
    } else {
      result.notImplemented();
    }
  }

  private void openDatabase(String dbName, Result result){
    this.result = result;
    String fileName = PathUtils.getDataDirectory(registrar.context()) +"/"+ dbName;
    if(this.db == null){
      this.db = Nitrite.builder().filePath(fileName).openOrCreate();

      // Result Result
      result.success("Opened database in "+ fileName +" successfully");
    } else {
      result.success(null);
    }
    
  }

  private void createDocument(String collectionName, String document, Result result){
    if(this.db != null){
      NitriteCollection collection = this.db.getCollection(collectionName);
      NitriteMapper nitriteMapper = new JacksonMapper();
      Document doc = nitriteMapper.parse(document);
      collection.insert(doc);
      
      // Return result
      result.success(doc);
    } else {
      result.error(ERROR_DB, "Could not open database", "Could Not insert document");
    }
  }

  private void find(String collectionName, int offset, int limit, Result result){
    if(this.db != null){
      NitriteCollection collection = this.db.getCollection(collectionName);
      Cursor results = collection.find(FindOptions.sort("_id", SortOrder.Descending).thenLimit(offset, limit));
      List<Document> data = Iterables.toList(results);
      if(data == null)
        data = new ArrayList<>();
      
      result.success(data);

    } else {
      result.error(ERROR_DB, "Could not open database", "Unable to find collection");
    }
  }

  private void findById(String collectionName, Long id, Result result){
    if(this.db != null){
      NitriteCollection collection = this.db.getCollection(collectionName);
      NitriteId _id = NitriteId.createId(id);
      Document doc = collection.getById(_id);

      if(doc == null)
        doc = new Document();
      
      result.success(doc);
    } else {
      result.error(ERROR_DB, "Could not open database", "Unable to find collection");
    }
  }

  private void deleteDocument(String collectionName, Long id, boolean emptyCollection, Result result){
    if(this.db != null){
      NitriteCollection collection = this.db.getCollection(collectionName);
      if(emptyCollection){
        collection.remove(Filters.ALL);
      } else {
        NitriteId _id = NitriteId.createId(id);
        Document doc = collection.getById(_id);
        collection.remove(doc);
      }
      
      result.success("Removed successfully");
    } else {
      result.error(ERROR_DB, "Could not open database", "Unable to remove");
    }
  }

  private void update(String collectionName, String document, boolean upsert, Result result){
    if(this.db != null){
      NitriteCollection collection = this.db.getCollection(collectionName);
      NitriteMapper nitriteMapper = new JacksonMapper();
      Document doc = nitriteMapper.parse(document);
      collection.update(doc, upsert);

      result.success(doc);
    } else {
      result.error(ERROR_DB, "Could not open database", "Unable to find collection");
    }
  }

  private void subscribe(final String collectionName, Result result){
    if(this.db != null){
      this.collectionChannel.setStreamHandler(
        new StreamHandler(){
          @Override
          public void onListen(Object args, final EventSink events){
            Log.w("NITRITE", "Adding Listener on : "+collectionName);
            NitriteCollection collection = db.getCollection(collectionName);
            ChangeListener listener = createCollectionChangeListener(events);
            collection.register(listener);
          }

          @Override
          public void onCancel(Object args){
            Log.w("NITRITE", "Cancelling listeners");
          }
        }
      );
      result.success(null);
    } else {
      result.error(ERROR_DB, "An error occured", "Error Occured");
    }
  }

  private void findWhere(String collectionName, String field, Object value, int limit, int offset, Result result){
    if(this.db != null){
      NitriteCollection collection = this.db.getCollection(collectionName);
      Cursor results = collection.find(Filters.eq(field, value), FindOptions.sort("_id", SortOrder.Descending).thenLimit(offset, limit));
      List<Document> data = Iterables.toList(results);
      if(data == null)
        data = new ArrayList<>();
      
      result.success(data);

    } else {
      result.error(ERROR_DB, "Could not open database", "Unable to find collection");
    }
  }

  ChangeListener createCollectionChangeListener(final EventSink events){
    return new ChangeListener(){
      @Override
      public void onChange(ChangeInfo changeInfo){
        Log.w("NITRITE","Change type event: " + changeInfo.getChangeType());
        events.success("Action = " +changeInfo.getChangeType());
      }
    };
  }

  private void handleSuccess(Map<String, Object> data){
    if(this.result != null){
      this.result.success(data);
      this.result = null;
    }
  }
}
