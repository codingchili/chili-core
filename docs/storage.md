# Storage

Implementations for persistent and not-so-persistent (aka in-memory) data stores. The great thing about the storage implementations is that they are easy to use and interchangeable. For example, during prototyping we might want something easy to setup. For testing something without external dependencies. For production something reliable, clustered or super fast?

The storage API's and query DSL are designed primarily for use with NoSQL databases and only support storing homogeneous objects. The storage implementations are standalone and are optional, it's entirely possible to use MongoDB for example directly, or with the Vert.x APIs.

The API is modeled around a "queryable" map and inspired by the Hazelcast IMap interface. This results in a very convenient object store with a fast lookup on the primary key, while also supporting queries with indexes.

All examples uses the `Account` class which implements `Storable`.

Storage implementations

|Class|Persistence|Scope|Description|
|---|---|---|---|
| HazelMap| memory/disk* | Cluster | Distributed cluster, backed by an IMap. |
| ElasticMap | memory/disk | Server | Elasticsearch high-level transport client.  |
| MongoDBMap | memory*/disk | Server | MongoDB async client driver. |
| IndexedMapPersisted | disk | JVM | CQEngine backed with SQLite persistence. |
| IndexedMapVolatile| memory| JVM | CQEngine with in memory store and indexes. |
| SharedMap | memory | JVM | Thread/Verticle concurrency safe plain map. |
| JsonMap | memory | JVM | No serialization required for JsonObjects. |
| PrivateMap | memory | Instance | A map that isn't shared across loaders. |

*) Not included in the free/community version.

Persistence types indicates how the data is stored and if it survives a restart.

|Type|Description|
|---|---|
| memory | data is stored in RAM - super fast but does not survive restarts. |
| disk | data is persisted to disk and survives restarts. |

Types of scope

|Type|Description|
|---|---|
| Cluster | requires clustering, Hazelcasts cluster members has access. |
| Server| runs in a separate process or machine, shared with authorized. |
| JVM | shared with services running in the same JVM. |
| Instance | Not shared, even if the same Map name is used. |

For JVM scoped persistence stores the stored data is shared between all clients. This requires that the storage is loaded with the same database identifier. The database identifier is constructed from the database name and the collection name. As an example, the `IndexedMapPersisted` stores data to `<database name>/<collection name>.db` which is an SQLite file.

### Loading a storage

Loading a storage implementation is done using a `StorageLoader`.

```java
new StorageLoader(context)
    .withPlugin(HazelMap.class)
    .withDB("appName", "accounts")
    .withValue(Account.class)
    .build(done -> {
        if (done.succeeded()) {
            AsyncStorage<Account> db = done.result();
        } else {
            // handle done.cause();
        }
    });
```

The value class has a single requirement, it must implement `Storable`.

```java
class Account implements Storable {
    private String name;
    
    @Override
    public String getId() {
        return name;         
    }
}
```

`getId` is an optional override, if not implemented the `hashCode` of the `Storable` will be used. In this case make sure to use a stable `hashCode` and preferably something associated with the stored objects, so that the key can be used for faster lookups.

It is important to implement `hashCode` and `equals` for the following storages

- IndexedMapPersisted
- IndexedMapVolatile

As these implementations are based on the Java collections API.

When using `HazelMap` the following method override is required for attributes that are being sorted on.

```java
@Override
public int compareToAttribute(Storable other, String attribute) {
    Account account = (Account) other;
    
    switch (attribute) {
        case "name":
            return name.compareTo(account.getName());
        default:
            return 0;
    }
}
```

This can also be solved with some trickery using the `Serializer.getValueByPath` but there are too many variables in this case to be solved with a default implementation.

### Storage API

The storage API is modeled after a simple Map, all methods are asynchronous to avoid blocking the event loop. Some storage implementations that use in-memory and does not block can complete directly. This is an implementation detail and the API is always used asynchronously. This means `Future<T>` and `Handler<AsyncResult<T>>`, see the Vert.x documentation for more information on how these work.

```java
// retrieve the object with the id of "key".
get("key", (done) -> {
    if (done.succeeded()) {
        Account account = done.result();
    } else {
        // handle done.cause();
    }
});

// put the account object but ignore the result.
put(account, (done) -> {});

// check if the storage contains an object with the given key.
contains("key", (done) -> {
    boolean exists = done.result();    
});

// adds the account if it does not already exist.
putIfAbsent(account, (done) -> {
    if (done.succeeded()) {
        // inserted successfully.
    } else {
        // failed, if caused by ValueAlreadyPresent the
        // value already exists and was not inserted.
    }
});

// updates the given value but only if it already exists.
update(account, (done) -> {
    if (done.succeeded()) {
        // the existing value was updated.
    } else {
        // failed, if caused by ValueMissingException
        // the value didn't not previously exist.
    }
});

// retrieves all values in the store with a lazy stream.
values(done -> {
    // done.result() is a Stream<Value> lazily evaluated
    // depending on the storage.
});

// remove all entries from the store.
clear(done -> {
    // if done.succeeded() all entires are cleared.
});

// retrieve the current number of entries in the store.
size((done) -> {
    int count = done.result();
});

// add an index for a regular attribute {"petstore": {owner: "jess"}}
addIndex("petstore.owner");

// add an index for a multi-valued attribute {"petstore": {petNames: ["kitty1", "kitty2"]}}
addIndex("petstore.petNames[]");
```

It is recommended to add all indexes to CQEngine based disk-persistence stores, before adding any objects. As indexes
are not loaded from the SQLite database on startup, as these require special accessor implementations, "Attributes".
If the application is shut down, started and an object is added without calling .addIndex that object will not be added
to any indexes and cannot be found using attributes for which indexes exists for.

To solve this, call `IndexedMapPersisted.reindex()` before instantiating the IndexedMapPersisted storage plugin. Objects
added before the index was added with `.addIndex` can then be re-indexed, this will be done the next time `.addIndex`
is called and this incurs a performance penalty as the whole collection will be re-indexed. To avoid this, add all
indexes any time the application is started.

### Query API

The query API is the same for all storage implementations.

```java
// some storage we already initialized.
AsyncStorage<Account> account = db;

Query<Account> query = db.query("username")
        .equalTo("admin")
        .and("email")
            .like("@root.com")
    .or("age")
        .between(32, 64)
        .matches("[0-9]*")
        .and("lastname")
            .startsWith("duda")
    .pageSize(32)
    .page(4)
    .orderBy("firstName")
    .order(SortOrder.ASCENDING)
    .name("super_advanced_account_query");
            
query.execute(done -> {
    // done.result() all 32 matching results on page 4.
    // ordered in asending order by their firstname.
});

query.poll(done -> {
    // done.result() contains matches to the query
    // the query will be executed every second.
}, () -> 1000);

```

Serializing a query to DSL, this does NOT escape inputs in any way - do **NOT** use for user input !!!
```java
new Query().on("cat.type")
    .in("siamese", "perser", "ragdoll")
        .and("cat.color").equalTo("white")
    .or("cat.lifestyle").in("amphibians", "wateranimal").matches("[water].*")
    .or("cat.age").between(0L, 100L).and("cat.name").startsWith("fl")
    .orderBy("cat.name").order(SortOrder.ASCENDING)
    .page(3).pageSize(24)
    .setName("findCatsQ")
    .toString();

/*
Output:

NAMED QUERY 'findCatsQ' QUERY 
	ON cat.type IN (siamese,perser,ragdoll) AND cat.color EQ white 
	OR cat.lifestyle IN (amphibians,wateranimal) REGEX([water].*) 
	OR cat.age BETWEEN 0 100 AND cat.name STARTSWITH fl 
ORDERBY cat.name ASCENDING PAGE 3 PAGESIZE 24 

 */
```

Sometime in the future it might be possible to escape inputs properly. For now this must be done **MANUALLY**.

### Query DSL

There is also a text-based query parser that can be used to send queries over the network or by reading from configuration. There is no support for prepared statements, if that is needed use the Query API instead.

Example query

```sql
NAMED QUERY 'findCats Query' ON cat.type 
    IN (siamese,perser,ragdoll) 
      AND cat.color EQ white
    OR cat.lifestyle IN (amphibians,wateranimal)    
        AND cat.address REGEX([water ].*)
    OR cat.age BETWEEN 0 100 
        AND cat.name STARTSWITH fl
ORDERBY cat.name ASCENDING PAGE 3 PAGESIZE 24
```

The query can be parsed with the `QueryParser`

```java
// some database we already initialized.
AsyncStorage<Account> accounts = db;

// The query parser needs a reference to the backing storage.
QueryParser parser = new QueryParser(db::query);

// parsing the expression returns a QueryBuilder<Account>
parser.parse(expression)
    .execute(done -> {
        // done.result() contains matching elements.
    });
```

### Custom implementations

Providing a custom implementation is easy, just implement the `AsyncStorage<T>` interface and the `QueryBuilder<T>`. To help with the implementation of the query interface `AbstractQueryBuilder<T>` can be used.

To conform to existing implementations make sure to extend test cases from `MapTestCases`.

### Versions

Aiming to keep up to the latest versions, current support is

|Storage|Version|
|---|---|
|ElasticSeach|7.3.0|
|MongoDB |4.0.8|
|Hazelcast|3.10.5|
|CQEngine|3.4.0|

Please submit a feature request with any ideas on how to improve the APIs or to request support for another storage.