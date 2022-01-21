#!/bin/bash

team1=ASageActionBot
team2=OCommMiningBot

mkdir -p logs
if test -f "logs/results.log"; then
  mv logs/results.log logs/results_old.log
fi
if test -f "logs/warnings.log"; then
  mv logs/warnings.log logs/warnings_old.log
fi
if test -f "logs/freezing.log"; then
  mv logs/freezing.log logs/freezing_old.log
fi
if test -f "logs/detailed_results.log"; then
  mv logs/detailed_results.log logs/detailed_results_old.log
fi
if test -f "logs/log.log"; then
  mv logs/log.log logs/log_old.log
fi
# rm -f logs/results.log
# rm -f logs/warnings.log
# rm -f logs/freezing.log
# rm -f logs/detailed_results.log
# rm -f logs/log.log

count=0
for i in nottestsmall squer jellyfish progress sandwich underground \
    intersection valley fortress rivers uncomfortable eckleburg colosseum #doubledoors newdoubledoors
do
  count=$[count+1]
  echo "Running map $count: $i"
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
done
echo "Grepping results"
grep -F "wins" logs/log.log >> logs/results.log
grep -E "vs. |wins" logs/log.log >> logs/detailed_results.log
grep -E "Warning,|vs. " logs/log.log >> logs/warnings.log
grep -E "Birth |vs. " logs/log.log >> logs/freezing.log
# Nottestsmall  : 20 x 20 : 400
# Squer         : 25 x 25 : 625
# Jellyfish     : 30 x 30 : 900
# Progress      : 30 x 30 : 900
# Sandwich      : 59 x 20 : 1180
# Underground   : 59 x 20 : 1180
# Intersection  : 49 x 25 : 1225
# Valley        : 37 x 37 : 1669
# Fortress      : 60 x 30 : 1800
# Rivers        : 55 x 45 : 2475
# Uncomfortable : 58 x 58 : 3364
# Eckleburg     : 60 x 60 : 3600
# Colosseum     : 60 x 60 : 3600