local c = redis.call("decr", ARGV[1])
if(c == 0) then
    redis.call("incr", ARGV[2])
    redis.call("del", ARGV[3])
end
 return