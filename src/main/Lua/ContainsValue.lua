local val = ARGV[1]
local values = redis.call("HVALS", ARGV[2])
for i, name in ipairs(values) do
    if name == val then
        return 1
    end
end
return 0