local change_counter = tonumber(ARGV[1])
local a = redis.call("GET", ARGV[2])
local current_number = tonumber(a)
if(change_counter ~= current_number) then
    return current_number
end
local deleted_num = redis.call("hdel", ARGV[3], ARGV[4])
redis.call("incr", ARGV[2])
return current_number