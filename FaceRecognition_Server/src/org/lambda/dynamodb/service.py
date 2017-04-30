# -*- coding: utf-8 -*-


def lambda_handler(event, context):
    # Your code goes here!
    e = event.get('e')
    pi = event.get('pi')
    return e + pi
