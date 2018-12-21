#!/usr/bin/env python3

import model

m = model.HaliteModel()
m.train_on_files('leaderboard_replays', 'teccles')
m.save(file_name='svm.svc')