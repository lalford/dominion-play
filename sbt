#!/bin/sh
test -f ~/.sbtconfig && . ~/.sbtconfig
java -Xms2G -Xmx2G -Xss1M -XX:+CMSClassUnloadingEnabled -XX:ReservedCodeCacheSize=2G -XX:MaxPermSize=1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode ${SBT_OPTS} -jar `dirname $0`/sbt-launch-0.13.6.jar "$@"
