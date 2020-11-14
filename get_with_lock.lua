
local key = KEYS[1]
local block_time = KEYS[2]
local value = redis.call('get', key)
local token_gen_key = 'token_gen'
if not value then
    local lease_key = 'lease:'..key
    local time_key = 'timeblock:'..key
    local time_lock = redis.call('get', time_key)
    if time_lock then
        return {false, false}
    else
        redis.call('incr', token_gen_key)
        local token = redis.call('get', token_gen_key)
        redis.call('set', lease_key, token)
        redis.call('set', time_key, "ok", "PX", block_time)
        return {false, token}
    end
else
    return {value, false}
end
