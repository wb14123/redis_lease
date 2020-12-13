
local key = '{'..KEYS[1]..'}'
local token = ARGV[1]
local block_time = ARGV[2]
local value = redis.call('get', key)
if not value then
    local lease_key = 'lease:'..key
    local time_key = 'timeblock:'..key
    local time_lock = redis.call('get', time_key)
    if time_lock then
        return {false, false}
    else
        redis.call('set', lease_key, token)
        redis.call('set', time_key, "ok", "PX", block_time)
        return {false, token}
    end
else
    return {value, false}
end
