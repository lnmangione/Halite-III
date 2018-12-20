#!/bin/sh

javac RuleBot.java
javac Dummy.java
javac GeneticBot.java

# Run GA for each generation
for seed in {1..5}
do
	#./halite --replay-directory replays/ -vvv --turn-limit 300 -s $seed --width 32 --height 32 "java RuleBot" "java Dummy"
	./halite --replay-directory replays/ -vvv --turn-limit 300 -s $seed --width 32 --height 32 "java GeneticBot" "java Dummy"
	#./halite --no-replay -vvv -s $gen --width $dim --height $dim "java GeneticBot 1" "java benchmark/DefaultParams"
done