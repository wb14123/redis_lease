import unittest
import hashlib
import redis
import os
import time


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
        cls.redis = redis.Redis(host='localhost', port=6379,
                encoding="utf-8", decode_responses=True)

    def setUp(self):
        self.redis.flushall()

    def test_no_key(self):
        [value, lease] = self._get('k')
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_set_get(self):
        [value, lease] = self._get('k')
        self._set('k', lease, 'v')
        [value, lease] = self._get('k')
        self.assertEqual('v', value)
        self.assertIsNone(lease)

    def test_set_without_lease(self):
        with self.assertRaises(redis.exceptions.ResponseError):
            self._set('k', '0', 'v')
        [value, lease] = self._get('k')
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_double_get_set(self):
        [v1, lease1] = self._get('k')
        [v2, lease2] = self._get('k')
        self.assertNotEqual(lease1, lease2)
        self.assertIsNotNone(lease1)
        self.assertIsNotNone(lease2)

        with self.assertRaises(redis.exceptions.ResponseError):
            self._set('k', lease1, 'v1')
        self._set('k', lease2, 'v2')
        [value, lease] = self._get('k')
        self.assertEqual('v2', value)
        self.assertIsNone(lease)

    def test_del_none(self):
        self._del('k')
        [value, lease] = self._get('k')
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_del_key(self):
        [value, lease] = self._get('k')
        self._set('k', lease, 'v')
        [value, lease] = self._get('k')
        self.assertEqual('v', value)
        self.assertIsNone(lease)
        self._del('k')
        [value, lease] = self._get('k')
        self.assertIsNone(value)
        self.assertIsNotNone(lease)

    def test_del_lease(self):
        [value, lease] = self._get('k')
        self._del('k')
        with self.assertRaises(redis.exceptions.ResponseError):
            self._set('k', lease, 'v1')


    def _get(self, key):
        return self.redis.evalsha(self.get_sha1, 1, key)

    def _set(self, key, lease, value):
        return self.redis.evalsha(self.set_sha1, 3, key, lease, value)

    def _del(self, key):
        return self.redis.evalsha(self.del_sha1, 1, key)


class GetWithLockTest(RedisLeaseTest):

    def test_double_get_with_lock(self):
        [v1, lease1] = self._get_with_lock('k', 1000)
        [v2, lease2] = self._get_with_lock('k', 1000)
        self.assertIsNone(v1)
        self.assertIsNotNone(lease1)
        self.assertIsNone(v2)
        self.assertIsNone(lease2)
        self._set('k', lease1, 'v1')
        [value, lease] = self._get('k')
        self.assertEqual('v1', value)
        self.assertIsNone(lease)

    def _get(self, key):
        time.sleep(0.005) # make sure the previous lock is timed out
        return self._get_with_lock(key, 1)

    def _get_with_lock(self, key, timeout):
        return self.redis.evalsha(self.get_with_lock_sha1, 2, key, timeout)



if __name__ == '__main__':
    unittest.main()
