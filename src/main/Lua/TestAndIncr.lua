if(redis.call("HEXISTS", ARGV[1], ARGV[2]) == 1) then
    redis.call("INCR", ARGV[3])
end
return