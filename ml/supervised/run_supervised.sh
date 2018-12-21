#!/bin/sh

# Run supervised for 500 games
for seed in {1..500}
do
	./halite --no-replay -vvv --turn-limit 300 -s $seed --width 32 --height 32 "python3 SVMBotLeaderboard.py" "python3 dummy.py"
done