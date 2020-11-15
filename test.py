import unittest
import hashlib
import redis
import os
import time
import uuid
from rediscluster import RedisCluster


def sha1file(filename):
    base_dir = os.path.dirname(os.path.realpath(__file__))
    full_path = os.path.join(base_dir, filename)
    with open(full_path, 'rb') as f:
        content = f.read()[:-1]
        sha1 = hashlib.sha1(content)
        return sha1.hexdigest()


class RedisLeaseTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.get_sha1 = sha1file('get.lua')
        cls.get_with_lock_sha1 = sha1file('get_with_lock.lua')
        cls.set_sha1 = sha1file('set.lua')
        cls.del_sha1 = sha1file('del.lua')
        cls.redis = cls._get_redis()

    def setUp(self):
        self.redis.flushall()

    def test_no_key(self):
        key = self._get_random_key()
        [value, lease] = self._get(key)
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_set_get(self):
        key = self._get_random_key()
        [value, lease] = self._get(key)
        self._set(key, lease, 'v')
        [value, lease] = self._get(key)
        self.assertEqual('v', value)
        self.assertIsNone(lease)

    def test_set_without_lease(self):
        key = self._get_random_key()
        with self.assertRaises(redis.exceptions.ResponseError):
            self._set(key, '0', 'v')
        [value, lease] = self._get(key)
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_double_get_set(self):
        key = self._get_random_key()
        [v1, lease1] = self._get(key)
        [v2, lease2] = self._get(key)
        self.assertNotEqual(lease1, lease2)
        self.assertIsNotNone(lease1)
        self.assertIsNotNone(lease2)

        with self.assertRaises(redis.exceptions.ResponseError):
            self._set(key, lease1, 'v1')
        self._set(key, lease2, 'v2')
        [value, lease] = self._get(key)
        self.assertEqual('v2', value)
        self.assertIsNone(lease)

    def test_del_none(self):
        key = self._get_random_key()
        self._del(key)
        [value, lease] = self._get(key)
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_del_key(self):
        key = self._get_random_key()
        [value, lease] = self._get(key)
        self._set(key, lease, 'v')
        [value, lease] = self._get(key)
        self.assertEqual('v', value)
        self.assertIsNone(lease)
        self._del(key)
        [value, lease] = self._get(key)
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_del_lease(self):
        key = self._get_random_key()
        [value, lease] = self._get(key)
        self._del(key)
        with self.assertRaises(redis.exceptions.ResponseError):
            self._set(key, lease, 'v1')

    @classmethod
    def _get_redis(cls):
        return redis.Redis(host='localhost', port=6379,
                encoding="utf-8", decode_responses=True)

    def _get(self, key):
        return self.redis.evalsha(self.get_sha1, 1, key, self._get_token())

    def _set(self, key, lease, value):
        return self.redis.evalsha(self.set_sha1, 1, key, lease, value)

    def _del(self, key):
        return self.redis.evalsha(self.del_sha1, 1, key)

    def _get_token(self):
        return str(uuid.uuid4())

    def _get_random_key(self):
        return self._get_token()


class GetWithLockTest(RedisLeaseTest):

    def test_double_get_with_lock(self):
        key = self._get_random_key()
        [v1, lease1] = self._get_with_lock(key, 1000)
        [v2, lease2] = self._get_with_lock(key, 1000)
        self.assertIsNone(v1)
        self.assertIsNotNone(lease1)
        self.assertIsNone(v2)
        self.assertIsNone(lease2)
        self._set(key, lease1, 'v1')
        [value, lease] = self._get(key)
        self.assertEqual('v1', value)
        self.assertIsNone(lease)

    def _get(self, key):
        time.sleep(0.005) # make sure the previous lock is timed out
        return self._get_with_lock(key, 1)

    def _get_with_lock(self, key, timeout):
        return self.redis.evalsha(self.get_with_lock_sha1, 1, key,
                self._get_token(), timeout)


class ClusterTest(RedisLeaseTest):
    @classmethod
    def _get_redis(cls):
        return RedisCluster(startup_nodes=[
                {"host": "127.0.0.1", "port": "30001"},
                {"host": "127.0.0.1", "port": "30002"},
                {"host": "127.0.0.1", "port": "30003"},
            ],decode_responses=True)

if __name__ == '__main__':
    unittest.main()
