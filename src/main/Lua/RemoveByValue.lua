local map = redis.call("HGETALL", ARGV[2])
local val = ARGV[1]
local key
for i, v in ipairs(map) do
    if i % 2 == 1 then
        key = v
    else
        if(v == val) then
            return redis.call("HDEL", ARGV[2], key)
        end
    end
end
return 0