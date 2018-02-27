#!/usr/bin/env python

# Copyright 2016 Google, Inc
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Analyzes text using the Google Cloud Natural Language API."""

import argparse
import json
import sys

import googleapiclient.discovery
import os
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/home/nishnik/hypertrack_backend/hypertrack-hackathon-d41bef2f7ec0.json"


def get_native_encoding_type():
    """Returns the encoding type that matches Python's native strings."""
    if sys.maxunicode == 65535:
        return 'UTF16'
    else:
        return 'UTF32'


def analyze_entities(text, encoding='UTF32'):
    body = {
        'document': {
            'type': 'PLAIN_TEXT',
            'content': text,
        },
        'encoding_type': encoding,
    }
    service = googleapiclient.discovery.build('language', 'v1')
    request = service.documents().analyzeEntities(body=body)
    response = request.execute()
    ents = response["entities"]
    ents_str = ""
    for ent in ents:
        ents_str += ent["name"]
        ents_str += " "
    return ents_str

text = "go to car mechanic"
result = analyze_entities(text)
print (result)

text = "buy medicines"
result = analyze_entities(text)
print (result)

text = "buy a mobile phone charger from arora "
result = analyze_entities(text)
print (result)


text = "remind me to buy some grocery from Sai Kripa store"
result = analyze_entities(text)
print (result)
