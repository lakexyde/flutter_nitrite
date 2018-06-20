import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_nitrite/flutter_nitrite.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new MainPage()
    );
  }
}

class MainPage extends StatefulWidget {
  @override
  _MainPageState createState() => new _MainPageState();
}

class _MainPageState extends State<MainPage> {
  TextEditingController _nameController = new TextEditingController();
  String _platformVersion = 'Unknown';
  String dbName = "";

  Map _result = {"name": "waiting"};
  

  @override
  initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterNitrite.platformVersion;
      
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted)
      return;

    setState(() {
      _platformVersion = platformVersion;
    });

    

  }

  void _saveInfo() async{
    var text = _nameController.text;
    
    if(text != ""){
      Map<String, dynamic> info = {"name": text};
      var res = await FlutterNitrite.createDocument("test", info);
      setState((){_result = res;});
      _nameController.clear();
    }
  }

  void _getCollection() async{
    List t = await FlutterNitrite.findWhere("test", "name", "No");
    print(t);
    setState(() {
      _result = t.first;
    });
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
        appBar: new AppBar(
          title: new Text('Plugin example app'),
          actions: <Widget>[
            new IconButton(
              icon: new Icon(Icons.refresh),
              onPressed: _getCollection,
            )
          ],
        ),
        floatingActionButton: new FloatingActionButton(
          child: new Icon(Icons.add),
          onPressed: () async{
            String p = await FlutterNitrite.openDatabase("test.db");
            FlutterNitrite.subscribe("test");
            setState((){
              dbName = p;
            });
          },
        ),
        body: new Container(
          alignment: Alignment.center,
          child: new Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              new Text('Running on: $_platformVersion\n'),
              new SizedBox(height: 20.0),
              new Text("$dbName"),
              new Padding(
                padding: const EdgeInsets.symmetric(vertical: 10.0, horizontal: 20.0),
                child: new TextField(
                  controller: _nameController,
                )
              ),
              new RaisedButton(
                onPressed: _saveInfo,
                child: new Text("Store name"),
              ),
              new SizedBox(height: 20.0),
              new Text("Insert Id: $_result"),
              new RaisedButton(
                onPressed: (){
                  Navigator.of(context).push(
                    new MaterialPageRoute(
                      builder: (BuildContext context) =>
                        new AnotherPage()
                    )
                  );
                },
                child: new Text("Go away"),
              ),
            ]
          ),
        ),
      );
  }
}

class AnotherPage extends StatefulWidget {
  @override
  _AnotherPageState createState() => new _AnotherPageState();
}

class _AnotherPageState extends State<AnotherPage> {
  StreamSubscription _collectionSubscription;
  TextEditingController _nameController = new TextEditingController();
  Map _result = {};

  @override
  void initState(){
    super.initState();

    _created();
  }

  void _created(){
    if(_collectionSubscription == null){
      _collectionSubscription = FlutterNitrite.subscribeToCollection.receiveBroadcastStream()
      .listen((t){
        print("======== $t");
      });
    } else {
      print("done ==== done");
    }
  }

  void _updateText(timer){
    // setState(() {
         
    //     });
    print("Chagedddd=====");
  }

  void _saveInfo() async{
    var text = _nameController.text;
    
    if(text != ""){
      Map<String, dynamic> info = {"name": text};
      var res = await FlutterNitrite.createDocument("test", info);
      setState((){_result = res;});
      _nameController.clear();
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      appBar: new AppBar(
        title: new Text("Sub Page")
      ),
      body: new Container(
        alignment: Alignment.center,
        child: new Column(
          children: <Widget>[
            new TextField(
              controller: _nameController
            ),
            new SizedBox(height: 20.0,),
            new RaisedButton(
              onPressed: _saveInfo,
              child: new Text("Store name"),

            ),
            new SizedBox(height: 20.0,),
            new Text("$_result")
          ],
        ),
      )
    );
  }
}
