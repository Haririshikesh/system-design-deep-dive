-- Redis Lua script for Token Bucket Algorithm
-- Provides atomicity to avoid race conditions.

local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

-- TTL representing how long the bucket must stay in Redis
local fill_time = capacity / refill_rate
local ttl = math.floor(fill_time * 2)

-- Fetch current values or initialize them
local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
  last_tokens = capacity
end

local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
  last_refreshed = 0
end

-- Calculate difference in time
local delta = math.max(0, now - last_refreshed)

-- Refill the bucket
local filled_tokens = math.min(capacity, last_tokens + (delta * refill_rate))

local allowed = 0
local new_tokens = filled_tokens

-- Check if we have enough tokens to allow this request
if filled_tokens >= requested then
  allowed = 1
  new_tokens = filled_tokens - requested
end

-- Save the new state back into Redis
redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

-- Return the boolean flag and remaining tokens
return { allowed, new_tokens }
