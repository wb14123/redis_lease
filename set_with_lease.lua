
local key = KEYS[1]
local lease = KEYS[2]
local value = KEYS[3]

local lease_key = "lease:", key
local server_lease = redis.call('get', lease_key)
if server_lease == lease then
    redis.call('set', key, value)
    return redis.status_reply("ok")
else
    return redis.error_reply("invalid lease token")
end
