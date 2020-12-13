
A Redis implementation for get/set/del values with lease. Described in paper [Scaling Memcache at Facebook](https://pdos.csail.mit.edu/6.824/papers/memcache-fb.pdf).

TLA+ specification for the algorithm:

* [Blog post](https://www.binwang.me/2020-11-02-Use-TLA+-to-Verify-Cache-Consistency.html)
* [Source code](https://github.com/wb14123/tla-cache)

**This is only a proof of concept. Not supposed to be used on any production environment.**

## Usage

Load scripts with `./load_scripts.sh <redis-host> <redis-port>`. It will show commands to get/set/del values.

For get command, it will return (value, nil) if it can find a value for the key, or (nil, lease) if it cannot find a value.

## Test

### Functional test

Start a Redis instance at `127.0.0.1:6379`. And a Redis cluster at `127.0.0.1:30001`, `127.0.0.1:30002`, `127.0.0.1:30003`. (You can do it by following [Redis cluster doc](https://redis.io/topics/cluster-tutorial), section "Creating a Redis Cluster using the create-cluster script").

Then load the Redis scripts with `./load_scripts.sh <redis-host> <redis-port>` to all the instances above.

Then run the following Python script to test:

```
python test.py
```

### Jepsen test

The Jepsen test sets up two Redis instances, one as a cache and one as a database. It will read and update key "foo", and check if the value in cache and database is consistent.

Setup a Jepsen cluster with two worker nodes as `redis-db` and `redis-cache`. You can do it by using Docker (the files are modified based on [Jepsen's Docker setup](https://github.com/jepsen-io/jepsen/tree/main/docker)):

```
cd jepsen-test/docker
export JEPSEN_ROOT="/home/wangbin/hobbi_source/redis_lease"
./bin/up --dev
```

Then go into the Jepsen console container by running:

```
./bin/console
```

The code of this repo should be under `/jepsen`. Go to `/jepsen/jepsen-test/jepsen.redis.lease` and run all the tests:

```
cd /jepsen/jepsen-test/jepsen.redis.lease
```

Run the tests with raw Redis get/set/del, this test should fail since raw operations cannot guarantee cache consistency:

```
lein run test --nodes-file ./nodes --concurrency 500
```

Run the tests with our Redis get/set/del scripts, this test should succeed because our algorithm makes sure the cache will be consistent with database:

```
lein run test --nodes-file ./nodes --concurrency 500 --lease
```

Run the tests with our Redis get/set/del scripts, and import cache failures. This test should fail because the algorithm cannot guarantee cache consistency if error happens during write to cache:

```
lein run test --nodes-file ./nodes --concurrency 500 --lease --fail
```
