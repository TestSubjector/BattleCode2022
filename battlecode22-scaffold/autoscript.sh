#!/bin/bash

team1=AByteBot
team2=AFleeingBot

mkdir -p logs

rm -f logs/results.log
rm -f logs/warnings.log
rm -f logs/freezing.log
rm -f logs/detailed_results.log
rm -f logs/log.log

for i in intersection eckleburg fortress colosseum jellyfish progress rivers \
    valley squer sandwich underground nottestsmall uncomfortable #doubledoors newdoubledoors
do
  echo "Running map $i"
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
done
echo "Grepping results"
grep -F "wins" logs/log.log >> logs/results.log
grep -E "vs.|wins" logs/log.log >> logs/detailed_results.log
grep -F "vs.|Warning" logs/log.log >> logs/warnings.log
grep -F "vs.|Birth round" logs/log.log >> logs/freezing.log