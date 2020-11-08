
local key = KEYS[1]
local value = redis.call('get', key)
local token_gen_key = 'token_gen'
if not value then
    local lease_key = 'lease:'..key
    redis.call('incr', token_gen_key)
    local token = redis.call('get', token_gen_key)
    redis.call('set', lease_key, token)
    return {false, token}
else
    return {value, false}
end
