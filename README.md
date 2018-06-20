# flutter_nitrite

A Nitrite Database wrapper for Flutter apps

#### Only supports Android for now.

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/platform-plugins/#edit-code).

# Getting Started

## Open or create a database
- import ```flutter_nitrite```
```
import 'package:flutter_nitrite/flutter_nitrite.dart';
```

- Then call the ```openDatabase``` method and pass in your database file as a string.
```
FlutterNitrite.openDatabase("test.db");
```
## Create a document
```
Map user = {
    "name": "Olalekan Oladipupo",
    "website": "https://github.com/lakexyde"
};

Map response = await FlutterNitrite.createDocument("users", user); 
```
It takes a ```Collection``` name as the first argument and a document as the second. Then returns the saved ```Document```. If the ```Collection``` doesn't exist, it creates it.
## Update a document
```
Map response = FlutterNitrite.update("collectionName", doc);
```
You can also pass in an optional name parameter ```upsert: true``` if you want the document to be inserted if it doesn't exist.
## Delete a document
```
String result = await FlutterNitrite.deleteDocument("CollectionName", 287497989079);
```
Takes a ```Collection``` name and the ```_id``` of the document. Returns a string message for the transaction.
## List items in a Collection
```
List response = await FlutterNitrite.find("collectionName");
```
- it also takes optional named parameters:
    ```
    offset; // defaults to 0
    limit; // defaults tp 15
    ```

## Find a ```Document``` by id
```
Map response = await FlutterNitrite.findById("collectionName", 235897485085);
```
It return the ```Document``` or null if it doesn't exist.
- Every created ```Document``` autogenerates a ```_id``` field in the database for indexing.
## Find a list of document by query
```
List response = await FlutterNitrite.findWhere("collectionName", "field", "value");
```
It returns a list documents that satisfy the given query. Also takes optional name parameters for ```offset``` and ```limit```. Good for pagination.