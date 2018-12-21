#!/usr/bin/env python3

import SVMBot


class SVMBotLeaderboard(SVMBot.SVMBot):
    def __init__(self):
        super().__init__("svm.svc")


if __name__ == '__main__':
    bot = SVMBotLeaderboard()
    bot.run()
