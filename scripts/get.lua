
local key = '{'..KEYS[1]..'}'
local token = ARGV[1]
local value = redis.call('get', key)
if not value then
    redis.replicate_commands()
    local lease_key = 'lease:'..key
    redis.call('set', lease_key, token)
    return {false, token}
else
    return {value, false}
end
