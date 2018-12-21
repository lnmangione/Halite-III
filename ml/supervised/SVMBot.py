
# Adapted from: https://halite.io/learn-programming-challenge/tutorials/ml-svm
#!/usr/bin/env python3

import os
from collections import defaultdict

import hlt
import model
from hlt import constants


class SVMBot:
    def __init__(self, weights):
        # Get the initial game state
        game = hlt.Game()
        game.ready("SVM-" + os.path.basename(weights))

        # During init phase: initialize the model and compile it
        my_model = model.HaliteModel(weights=weights)

        self.my_model = my_model
        self.game = game

    def run(self):
        # Some minimal state to say when to go home
        go_home = defaultdict(lambda: False)
        while True:
            self.game.update_frame()
            me = self.game.me  # Here we extract our player metadata from the game state
            game_map = self.game.game_map  # And here we extract the map metadata
            other_players = [p for pid, p in self.game.players.items() if pid != self.game.my_id]

            command_queue = []

            for ship in me.get_ships():  # For each of our ships
                # Did not machine learn going back to base. Manually tell ships to return home
                if ship.position == me.shipyard.position:
                    go_home[ship.id] = False
                elif go_home[ship.id] or ship.halite_amount == constants.MAX_HALITE:
                    go_home[ship.id] = True
                    movement = game_map.get_safe_move(game_map[ship.position], game_map[me.shipyard.position])
                    if movement is not None:
                        game_map[ship.position.directional_offset(movement)].mark_unsafe(ship)
                        command_queue.append(ship.move(movement))
                    else:
                        ship.stay_still()
                    continue

                # Use machine learning to get a move
                ml_move = self.my_model.predict_move(ship, game_map, me, other_players, self.game.turn_number)
                if ml_move is not None:
                    movement = game_map.get_safe_move(game_map[ship.position],
                                                      game_map[ship.position.directional_offset(ml_move)])
                    if movement is not None:
                        game_map[ship.position.directional_offset(movement)].mark_unsafe(ship)
                        command_queue.append(ship.move(movement))
                        continue
                ship.stay_still()

            # Spawn some more ships
            if me.halite_amount >= constants.SHIP_COST and \
                    not game_map[me.shipyard].is_occupied and len(me.get_ships()) == 0:
                command_queue.append(self.game.me.shipyard.spawn())

            if self.game.turn_number == 299:
                with open("SVM.txt","a") as file:
                    file.write(str(me.halite_amount - 4000) + "\n")


            self.game.end_turn(command_queue)  # Send our moves back to the game environment
