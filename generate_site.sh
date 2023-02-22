#!/bin/bash

set -xe

(cd core/src/site/asy; make)
mvn site --pl core

