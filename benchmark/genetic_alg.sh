#!/bin/sh

GENERATIONS=400
INDIVIDUALS=20

javac MyBot.java
# javac v17/MyBot.java
javac benchmark/DefaultParams.java
javac pep/InitializeGA.java
javac pep/ResumeGA.java
javac pep/GA.java

java pep/InitializeGA

# Run GA for each generation
for gen in {250..500}
do
	# Each individual plays 5 matches, one for each map size
	for dim in 32
	do
		./halite --replay-directory replays/ -vvv -s $gen --width $dim --height $dim "java MyBot 1" "java benchmark/DefaultParams"
		#./halite --replay-directory replays/ -vvv -s $gen --width $dim --height $dim "java MyBot 1" "java benchmark/DefaultParams" "java benchmark/DefaultParams" "java benchmark/DefaultParams"
		for ind in {1..19}
		do
			./halite --no-replay -vvv -s $gen --width $dim --height $dim "java MyBot 1" "java benchmark/DefaultParams"
		#	./halite --no-replay -vvv -s $gen --width $dim --height $dim "java MyBot 1" "java benchmark/DefaultParams" "java benchmark/DefaultParams" "java benchmark/DefaultParams"
		done
	done
	java pep/GA
done