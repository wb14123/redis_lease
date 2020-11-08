#!/bin/bash

set -e

script=`cat get.lua`
get_sha=`redis-cli script load "$script"`
script=`cat get_with_lock.lua`
get_lock_sha=`redis-cli script load "$script"`
script=`cat set.lua`
set_sha=`redis-cli script load "$script"`
script=`cat del.lua`
del_sha=`redis-cli script load "$script"`

echo "Scripts load sucessful. Usage:"
echo "Get key: redis-cli evalsha $get_sha 1 <key>"
echo "Get key with lock: redis-cli evalsha $get_lock_sha 2 <key> <lock_timeout>"
echo "Set key: redis-cli evalsha $set_sha 3 <key> <lease> <value>"
echo "Del key: redis-cli evalsha $del_sha 1 <key>"
