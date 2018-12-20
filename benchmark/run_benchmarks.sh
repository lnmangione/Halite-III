#!/bin/sh

javac RuleBot.java
javac Dummy.java
javac GeneticBot.java

# Run benchmarks 500 games
for seed in {1..500}
do
	./halite --no-replay -vvv --turn-limit 300 -s $seed --width 32 --height 32 "java RuleBot" "java Dummy"
	./halite --no-replay -vvv --turn-limit 300 -s $seed --width 32 --height 32 "java GeneticBot" "java Dummy"
done