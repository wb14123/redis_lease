#!/bin/bash

set -e

script=`cat get_with_lease.lua`
get_sha=`redis-cli script load "$script"`
script=`cat set_with_lease.lua`
set_sha=`redis-cli script load "$script"`
script=`cat del_with_lease.lua`
del_sha=`redis-cli script load "$script"`

echo "Scripts load sucessful. Usage:"
echo "Get key: redis-cli evalsha $get_sha 1 <key>"
echo "Set key: redis-cli evalsha $set_sha 3 <key> <lease> <value>"
echo "Del key: redis-cli evalsha $del_sha 1 <key>"
