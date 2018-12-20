#!/bin/sh

javac RuleBot.java
javac Dummy.java

# Run GA for each generation
for seed in {1..5}
do
	./halite --replay-directory replays/ -vvv --turn-limit 300 -s $seed --width 32 --height 32 "java RuleBot 1" "java Dummy"
	#./halite --no-replay -vvv -s $gen --width $dim --height $dim "java MyBot 1" "java benchmark/DefaultParams"
done