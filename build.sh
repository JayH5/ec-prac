#!/bin/bash
mkdir -p bin
if [ "$(uname)" == "Darwin" ]; then
  EXEC='/usr/libexec/java_home -v 1.7.0 --exec'
fi
$EXEC javac -d bin -cp 'libs/*' src/*
