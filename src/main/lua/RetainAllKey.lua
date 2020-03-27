local cnt = 0
local a = redis.call("HKEYS", ARGV[1])
for i, name in ipairs(a) do
    local f = 0
    for j = 3, #ARGV, 1 do
        if(name == ARGV[j]) then
            f = 1
        end
    end
    if(f == 0) then
        cnt = cnt + redis.call("HDEL", ARGV[1], name)
    end
end
if(cnt > 0) then
    redis.call("INCR", ARGV[2])
end
 return cnt