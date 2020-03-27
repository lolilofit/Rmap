local cnt = 0
local map = redis.call("HGETALL", ARGV[1])
local key
for i, v in ipairs(map) do
    if i % 2 == 1 then
        key = v
    else
        for j = 2, #ARGV, 1 do
            if(v == ARGV[j]) then
                cnt = cnt + redis.call("HDEL", ARGV[1], key)
            end
        end
    end
end
return cnt