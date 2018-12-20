#!/bin/sh

GENERATIONS=200
INDIVIDUALS=20

javac GeneticBot.java
javac Dummy.java
javac pep/InitializeGA.java
javac pep/ResumeGA.java
javac pep/GA.java

java pep/InitializeGA

# Run GA for each generation
for gen in {1000..1200}
do
	./halite --replay-directory replays/ -vvv --turn-limit 300 -s $gen --width 32 --height 32 "java GeneticBot 1" "java Dummy"
	for ind in {1..19}
	do
		./halite --no-replay -vvv --turn-limit 300 -s $gen --width 32 --height 32 "java GeneticBot 1" "java Dummy"
	done
	java pep/GA
done