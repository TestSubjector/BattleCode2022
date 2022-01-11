#!/bin/bash

team1=OBFSTestBot
team2=ABFSBot

mkdir -p logs

rm -f logs/results.log
rm -f logs/warnings.log
rm -f logs/log.log

for i in intersection eckleburg maptestsmall doubledoors
do
  echo "Running map $i"
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
done
echo "Grepping results"
grep wins    logs/log.log >> logs/results.log
grep Warning logs/log.log >> logs/warnings.log