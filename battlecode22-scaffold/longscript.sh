#!/bin/bash

team1=ADepleteBot
for team2 in OProductionBot OEcoBot OFleeingBot OEfficientBot OMicroBot OActualCombatBot OCombatBot ONewFrontierBot
do
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

for i in nottestsmall squer jellyfish progress fortress sandwich underground \
    intersection valley rivers uncomfortable eckleburg colosseum #doubledoors newdoubledoors
do
  echo "$team2: Running map $i"
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
# Fortress      : 60 x 30 : 1800
# Sandwich      : 59 x 20 : 1180
# Underground   : 59 x 20 : 1180
# Intersection  : 49 x 25 : 1225
# Valley        : 37 x 37 : 1669
# Rivers        : 55 x 45 : 2475
# Uncomfortable : 58 x 58 : 3364
# Eckleburg     : 60 x 60 : 3600
# Colosseum     : 60 x 60 : 3600
done