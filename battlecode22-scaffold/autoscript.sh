#!/bin/bash

team1=AFleeingBot
team2=OFleeingDummyBot

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

for i in intersection eckleburg fortress colosseum jellyfish progress rivers \
    valley squer sandwich underground nottestsmall uncomfortable #doubledoors newdoubledoors
do
  echo "Running map $i"
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
done
echo "Grepping results"
grep -F "wins" logs/log.log >> logs/results.log
grep -E "vs. |wins" logs/log.log >> logs/detailed_results.log
grep -F "vs. |Warning," logs/log.log >> logs/warnings.log
grep -F "vs. |Birth round " logs/log.log >> logs/freezing.log