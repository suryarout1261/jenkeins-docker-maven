#!/bin/bash
set -e
echo "====== BUILDING APPLICATION ======"
mvn clean compile -B
echo "====== BUILD COMPLETE ======"

