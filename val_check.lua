local val = ARGV[1]
local values = redis.call('HVALS', 'user:1000')

for i, name in ipairs(values) do
	if(name ~= val) then
		return 1;
	end
end
return 0


local val = "12" local values = redis.call("HVALS", "user:1000") for i, name in ipairs(values) do if name == val then return 1 end end return 0


EVAL 'local val = ARGV[1] local values = redis.call("HVALS", "user:1000") for i, name in ipairs(values) do if name == val then return 1 end end return 0' 0 '12'