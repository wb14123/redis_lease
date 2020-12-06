
A Redis implementation for get/set/del values with lease. Described in paper [Scaling Memcache at Facebook](https://pdos.csail.mit.edu/6.824/papers/memcache-fb.pdf).

TLA+ specification for the algorithm:

* [Blog post](https://www.binwang.me/2020-11-02-Use-TLA+-to-Verify-Cache-Consistency.html)
* [Source code](https://github.com/wb14123/tla-cache)

**This is only a proof of concept. Not supposed to be used on any production environment.**

## Usage

Load scripts with `./load_scripts.sh`. It will show commands to get/set/del values.

For get command, it will return (value, nil) if it can find a value for the key, or (nil, lease) if it cannot find a value.

## Test

### Functional test

```
python test.py
```

### Jepsen test

Setup a Jepsen cluster with two woker nodes as `redis-db` and `redis-cache`, then use the code under `jepsen-test/jepsen.redis.lease` and run

```
lein run test --nodes-file ./nodes --concurrency 500
```
