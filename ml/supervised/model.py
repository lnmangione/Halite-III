# Adapted from: https://halite.io/learn-programming-challenge/tutorials/ml-svm

import pickle
import random
import time

import numpy as np
from sklearn.svm import SVC
from tqdm import tqdm

import hlt
import parse
from hlt import constants
from hlt import positionals


class HaliteModel:
    MAX_FILES = 100
    DIRECTION_ORDER = [positionals.Direction.West,
                       positionals.Direction.North,
                       positionals.Direction.East,
                       positionals.Direction.South]
    MOVE_TO_DIRECTION = {
        "o": positionals.Direction.Still,
        "w": positionals.Direction.West,
        "n": positionals.Direction.North,
        "e": positionals.Direction.East,
        "s": positionals.Direction.South}
    OUTPUT_TO_MOVE = {
        0: "o",
        1: "w",
        2: "n",
        3: "e",
        4: "s"}
    MOVE_TO_OUTPUT = {v: k for k, v in OUTPUT_TO_MOVE.items()}

    def __init__(self, weights=None):
        if weights is not None:
            with open(weights, 'rb') as f:
                self.model = pickle.load(f)
        else:
            self.model = SVC(class_weight='balanced')

    def train_on_files(self, replay_folder, player_name):
        game_data = parse.parse_replay_folder(replay_folder, player_name, max_files=self.MAX_FILES)

        print("Processing Game States")
        game_states = []
        for g in game_data:
            turn_number = 0
            for game_map, moves, ships, other_ships, dropoffs, other_dropoffs in g:
                turn_number += 1
                for ship in list(ships.values()):
                    if random.random() < .25:
                        game_states.append((game_map, moves, ships, other_ships, dropoffs,
                                            other_dropoffs, turn_number, ship))

        print("Generating Training Data")
        data, labels = [], []
        for game_map, moves, ships, other_ships, dropoffs, other_dropoffs, turn_number, ship in tqdm(game_states):
            move = "o" if ship.id not in moves else moves[ship.id]
            # Throw away movements that take us closer to base. We will let logic take care of returning to base
            if move is not "o" and (
                    game_map.calculate_distance(ship.position.directional_offset(self.MOVE_TO_DIRECTION[move]),
                                                dropoffs[0].position) <
                    game_map.calculate_distance(ship.position, dropoffs[0].position)
            ):
                continue

            move_id = self.MOVE_TO_OUTPUT[move]
            for rot in range(4):  # do all 4 rotations for each game state
                data.append(self.input_for_ship(game_map,
                                                ship,
                                                [s2.position for s2 in ships.values() if s2.id != ship.id],
                                                [s2.position for s2 in other_ships.values()],
                                                [d.position for d in dropoffs],
                                                [d.position for d in other_dropoffs],
                                                turn_number,
                                                rotation=rot))
                labels.append(np.array(move_id))
                move_id = 0 if move_id == 0 else (move_id % 4) + 1
        data = np.array(data)
        labels = np.array(labels)

        print("Number of Datapoints: {}".format(len(data)))
        print("Number of Features: {}".format(len(data[0])))

        self.train(data, labels)

    def train(self, data, moves):
        print("Training Model")
        self.model.fit(data, moves)

    # Generate the feature vector
    def input_for_ship(self, game_map, ship, my_other_ships, other_ships, my_dropoffs, other_dropoffs, turn_number,
                       rotation=0):
        result = []

        # game turn
        percent_done = turn_number / constants.MAX_TURNS
        result.append(percent_done)

        # Local area stats
        for objs in [my_other_ships, other_ships, my_dropoffs, other_dropoffs]:
            objs_directions = []
            for d in self.DIRECTION_ORDER:
                objs_directions.append(int(game_map.normalize(ship.position.directional_offset(d)) in objs))
            result += self.rotate_direction_vector(objs_directions, rotation)

        # directions to highest halite cells at certain distances
        for distance in range(1, 13):
            max_halite_cell = self.max_halite_within_distance(game_map, ship.position, distance)
            halite_directions = self.generate_direction_vector(game_map, ship.position, max_halite_cell)
            result += self.rotate_direction_vector(halite_directions, rotation)

        # directions to closest drop off
        closest_dropoff = my_dropoffs[0]
        for dropoff in my_dropoffs:
            if game_map.calculate_distance(ship.position, dropoff) < game_map.calculate_distance(ship.position,
                                                                                                 closest_dropoff):
                closest_dropoff = dropoff
        dropoff_directions = self.generate_direction_vector(game_map, ship.position, closest_dropoff)
        result += self.rotate_direction_vector(dropoff_directions, rotation)

        # local area halite
        local_halite = []
        for d in self.DIRECTION_ORDER:
            local_halite.append(game_map[game_map.normalize(ship.position.directional_offset(d))].halite_amount / 1000)
        result += self.rotate_direction_vector(local_halite, rotation)

        # current cell halite indicators
        for i in range(0, 200, 50):
            result.append(int(game_map[ship.position].halite_amount <= i))
        result.append(game_map[ship.position].halite_amount / 1000)
        return result

    def predict_move(self, ship, game_map, me, other_players, turn_number):
        other_ships = [s.position for s in me.get_ships() if s.id != ship.id]
        opp_ships = [s.position for p in other_players for s in p.get_ships()]
        my_dropoffs = [d.position for d in list(me.get_dropoffs()) + [me.shipyard]]
        opp_dropoffs = [d.position for p in other_players for d in p.get_dropoffs()] + \
                       [p.shipyard.position for p in other_players]
        data = np.array(self.input_for_ship(game_map,
                                            ship,
                                            other_ships,
                                            opp_ships,
                                            my_dropoffs,
                                            opp_dropoffs,
                                            turn_number))
        data = data.reshape(1, -1)
        model_output = self.model.predict(data)[0]
        return self.MOVE_TO_DIRECTION[self.OUTPUT_TO_MOVE[model_output]]

    def save(self, file_name=None):
        if file_name is None:
            file_name = "model_weights_%f.svc" % time.time()
        with open(file_name, 'wb') as f:
            pickle.dump(self.model, f)

    # finds cell with max halite within certain distance of location
    def max_halite_within_distance(self, game_map, location, distance):
        max_halite_cell = location
        max_halite = 0
        for dx in range(-distance, distance + 1):
            for dy in range(-distance, distance + 1):
                loc = game_map.normalize(location + hlt.Position(dx, dy))
                if game_map.calculate_distance(location, loc) > distance:
                    continue

                # pick cell with max halite
                cell_halite = game_map[loc].halite_amount
                if cell_halite > max_halite:
                    max_halite_cell = loc
                    max_halite = cell_halite
        return max_halite_cell

    # generate vector that tells which directions to go to get from ship_location to target
    def generate_direction_vector(self, game_map, ship_location, target):
        directions = []
        for d in self.DIRECTION_ORDER:
            directions.append(
                int(game_map.calculate_distance(game_map.normalize(ship_location.directional_offset(d)), target) <
                    game_map.calculate_distance(ship_location, target)))
        return directions

    def rotate_direction_vector(self, direction_vector, rotations):
        for i in range(rotations):
            direction_vector = [direction_vector[-1]] + direction_vector[:-1]
        return direction_vector
