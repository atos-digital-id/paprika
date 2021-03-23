#!/bin/bash

set -xe

rm -rf docs
(cd core/src/site/asy; make)
mvn site --pl core

