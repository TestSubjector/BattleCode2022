#!/bin/bash
set -o errexit

team1=AFleeingBot
team2=OFleeingDummyBot

mkdir -p logs

rm -f logs/results.log
rm -f logs/warnings.log
rm -f logs/freezing.log
rm -f logs/detailed_results.log
rm -f logs/log1.log
rm -f logs/log2.log
rm -f logs/log.log

for i in intersection eckleburg fortress colosseum jellyfish progress rivers \
    valley squer sandwich underground nottestsmall uncomfortable #doubledoors newdoubledoors

do
  echo "Running map $i"
  (trap 'kill 0' SIGINT;
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log1.log &
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log2.log
  )
  wait
done

echo "Grepping results"
grep -F "wins" logs/log1.log >> logs/results.log
echo -e "===========\n" >> logs/results.log
grep -F "wins" logs/log2.log >> logs/results.log
grep -E "vs. | wins" logs/log1.log >> logs/detailed_results.log
echo -e "===========\n" >> logs/results.log
grep -E "vs. | wins" logs/log2.log >> logs/detailed_results.log
grep -F "Warning" logs/log1.log >> logs/warnings.log
echo -e "===========\n" >> logs/results.log
grep -F "Warning" logs/log2.log >> logs/warnings.log
grep -F "Birth round" logs/log1.log >> logs/freezing.log
echo -e "===========\n" >> logs/results.log
grep -F "Birth round" logs/log2.log >> logs/freezing.log