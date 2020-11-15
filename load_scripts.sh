#!/bin/bash

set -e

load_for_instance() {
	host=$1
	port=$2
	echo "Load script for $host:$port"
	REDIS_CLI="redis-cli -h $host -p $port"
	script=`cat get.lua`
	get_sha=`$REDIS_CLI script load "$script"`
	script=`cat get_with_lock.lua`
	get_lock_sha=`$REDIS_CLI script load "$script"`
	script=`cat set.lua`
	set_sha=`$REDIS_CLI script load "$script"`
	script=`cat del.lua`
	del_sha=`$REDIS_CLI script load "$script"`
}

load_for_instance '127.0.0.1' 6379
load_for_instance '127.0.0.1' 30001
load_for_instance '127.0.0.1' 30002
load_for_instance '127.0.0.1' 30003

echo "Scripts load sucessful. Usage:"
echo "Get key: redis-cli evalsha $get_sha 1 <key> <uniq_id>"
echo "Get key with lock: redis-cli evalsha $get_lock_sha 1 <key> <uniq_id> <lock_timeout>"
echo "Set key: redis-cli evalsha $set_sha 1 <key> <lease> <value>"
echo "Del key: redis-cli evalsha $del_sha 1 <key>"
