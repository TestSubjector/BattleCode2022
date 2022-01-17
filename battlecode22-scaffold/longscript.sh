#!/bin/bash

team1=ATopTenBot
count1=0
for team2 in OGrindstoneBot OFoolsGoldNewBot 
# OPageOneBot OGoldenBot OPaladinBot ODepleteBot OProductionBot OEcoBot OFleeingBot OEfficientBot OMicroBot OActualCombatBot OCombatBot
do
rm -rf logs/${team1}_vs_${team2}/
mkdir -p logs/${team1}_vs_${team2}
# if test -f "logs/${team2}_results.log"; then
#   mv logs/results.log logs/results_old.log
# fi
# if test -f "logs/warnings.log"; then
#   mv logs/warnings.log logs/warnings_old.log
# fi
# if test -f "logs/freezing.log"; then
#   mv logs/freezing.log logs/freezing_old.log
# fi
# if test -f "logs/detailed_results.log"; then
#   mv logs/detailed_results.log logs/detailed_results_old.log
# fi
# if test -f "logs/log.log"; then
#   mv logs/log.log logs/log_old.log
# fi
count1=$[count1+1]
count2=0
echo "Team number $count1: $team2, is processing:"
for i in nottestsmall squer jellyfish progress sandwich underground \
    intersection valley fortress rivers uncomfortable eckleburg colosseum #doubledoors newdoubledoors
do
  count2=$[count2+1]
  echo "$team2: Running map $count2: $i"
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/${team1}_vs_${team2}/log.log
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/${team1}_vs_${team2}/log.log
done
echo "Grepping results"
grep -F "wins" logs/${team1}_vs_${team2}/log.log >> logs/${team1}_vs_${team2}/results.log
grep -E "vs. |wins" logs/${team1}_vs_${team2}/log.log >> logs/${team1}_vs_${team2}/detailed_results.log
grep -E "Warning,|vs. " logs/${team1}_vs_${team2}/log.log >> logs/${team1}_vs_${team2}/warnings.log
grep -E "Birth |vs. " logs/${team1}_vs_${team2}/log.log >> logs/${team1}_vs_${team2}/freezing.log
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
done