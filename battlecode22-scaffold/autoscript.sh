#!/bin/bash
start_time=$SECONDS
team1=AMapBot
team2=OSplitBot
# team2=OPerfectProducerBot

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
if test -f "logs/log1.log"; then
  mv logs/log1.log logs/log1_old.log
fi
if test -f "logs/log2.log"; then
  mv logs/log2.log logs/log2_old.log
fi
# rm -f logs/results.log
# rm -f logs/warnings.log
# rm -f logs/freezing.log
# rm -f logs/detailed_results.log
# rm -f logs/log1.log
# rm -f logs/log2.log

count=0
for i in sandwich underground \
    intersection stronghold valley snowflake dodgeball fortress chessboard nyancat rivers highway panda uncomfortable eckleburg colosseum #  \
do
  count=$[count+1]
  if test $count -eq 1; then
    echo "Running map $count: $i"
  else
    echo -e "\e[1A\e[KRunning map $count: $i"
  fi
  # ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
  # ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log.log
  (trap 'kill 0' SIGINT;
  ./gradlew -PteamA=$team1 -PteamB=$team2 -Pmaps=$i -PprofilerEnabled=false run >> logs/log1.log &
  ./gradlew -PteamA=$team2 -PteamB=$team1 -Pmaps=$i -PprofilerEnabled=false run >> logs/log2.log
  )
  wait
done
echo "Grepping results"
# grep -F "wins" logs/log.log >> logs/results.log
# grep -E "vs. |wins" logs/log.log >> logs/detailed_results.log
# grep -E "Warning,|vs. " logs/log.log >> logs/warnings.log
# grep -E "Birth |vs. " logs/log.log >> logs/freezing.log
grep -F "wins" logs/log1.log >> logs/results.log
echo -e "===========\n" >> logs/results.log
grep -F "wins" logs/log2.log >> logs/results.log
grep -E "vs. |wins" logs/log1.log >> logs/detailed_results.log
echo -e "===========\n" >> logs/results.log
grep -E "vs. |wins" logs/log2.log >> logs/detailed_results.log
grep -E "Warning,|vs. " logs/log1.log >> logs/warnings.log
echo -e "===========\n" >> logs/results.log
grep -E "Warning,|vs. " logs/log2.log >> logs/warnings.log
grep -E "Birth |vs. " logs/log1.log >> logs/freezing.log
echo -e "===========\n" >> logs/results.log
grep -E "Birth |vs. " logs/log2.log >> logs/freezing.log 
elapsed=$(( SECONDS - start_time ))
eval "echo Elapsed time: $(date -ud "@$elapsed" +'$((%s/3600/24)) days %H hr %M min %S sec')"
#1: Nottestsmall  : 20 x 20 : 400
#2: spine         : 21 x 21 : 441
#3: Squer         : 25 x 25 : 625
#4: equals        : 30 x 30 : 900
#5: Jellyfish     : 30 x 30 : 900
#6: Progress      : 30 x 30 : 900
#7: tower         : 30 x 30 : 900
#8: collaboration : 38 x 25 : 950
#9: pillars       : 31 x 31 : 961
#10: Sandwich      : 59 x 20 : 1180
#11: Underground   : 59 x 20 : 1180
#12: Intersection  : 49 x 25 : 1225
#13: stronghold    : 41 x 31 : 1271
#14: Valley        : 37 x 37 : 1669
#15: snowflake     : 41 x 43 : 1763
#16: dodgeball     : 60 x 30 : 1800
#17: Fortress      : 60 x 30 : 1800
#18: chessboard    : 47 x 47 : 2209
#19: nyancat       : 50 x 45 : 2250
#20: Rivers        : 55 x 45 : 2475
#21: highway       : 50 x 50 : 2500
#22: panda         : 60 x 45 : 2700
#23: Uncomfortable : 58 x 58 : 3364
#24: Eckleburg     : 60 x 60 : 3600
#25: Colosseum     : 60 x 60 : 3600

#nottestsmall spine squer equals jellyfish progress tower collaboration pillars