
local key = '{'..KEYS[1]..'}'
local lease_key = "lease:"..key
local time_key = "timeblock"..key
redis.call('del', lease_key)
redis.call('del', time_key)
redis.call('del', key)
return redis.status_reply("ok")
