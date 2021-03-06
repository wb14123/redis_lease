
local key = '{'..KEYS[1]..'}'
local lease = ARGV[1]
local value = ARGV[2]

local lease_key = "lease:"..key
local server_lease = redis.call('get', lease_key)
local time_key = "timeblock:"..key
if server_lease == lease then
    redis.call('set', key, value)
    redis.call('del', time_key)
    return redis.status_reply("ok")
else
    return redis.error_reply("invalid lease token")
end
