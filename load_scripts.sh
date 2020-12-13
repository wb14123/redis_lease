#!/bin/bash

set -e

host=$1
port=$2

echo "Load script for $host:$port"
REDIS_CLI="redis-cli -h $host -p $port"
script=`cat scripts/get.lua`
get_sha=`$REDIS_CLI script load "$script"`
script=`cat scripts/get_with_lock.lua`
get_lock_sha=`$REDIS_CLI script load "$script"`
script=`cat scripts/set.lua`
set_sha=`$REDIS_CLI script load "$script"`
script=`cat scripts/del.lua`
del_sha=`$REDIS_CLI script load "$script"`

echo "Scripts load sucessful. Usage:"
echo "Get key: redis-cli evalsha $get_sha 1 <key> <uniq_id>"
echo "Get key with lock: redis-cli evalsha $get_lock_sha 1 <key> <uniq_id> <lock_timeout>"
echo "Set key: redis-cli evalsha $set_sha 1 <key> <lease> <value>"
echo "Del key: redis-cli evalsha $del_sha 1 <key>"
