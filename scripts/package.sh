#!/bin/bash
set -e
echo "====== PACKAGING APPLICATION ======"
mvn package -DskipTests -B
echo "====== PACKAGING COMPLETE ======"
ls -lh target/*.jar

