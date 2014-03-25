#!/bin/bash
if [ "$(uname)" == "Darwin" ]; then
  EXEC='/usr/libexec/java_home -v 1.7.0 --exec'
fi
$EXEC appletviewer EA-TSP2.htm
