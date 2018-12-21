import hlt
from hlt import constants
from hlt.positionals import Direction
import random
import logging

# GAME BEGIN

game = hlt.Game()
game.ready("Dummy")

# GAME LOOP

while True:
    game.update_frame()
    command_queue = []
    game.end_turn(command_queue)