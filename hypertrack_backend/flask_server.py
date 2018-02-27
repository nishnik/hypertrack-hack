'''
Simple web service wrapping a Word2Vec as implemented in Gensim
Example call: curl http://127.0.0.1:5000/wor2vec/n_similarity/ws1=Sushi&ws1=Shop&ws2=Japanese&ws2=Restaurant
@TODO: Add more methods
@TODO: Add command line parameter: path to the trained model
@TODO: Add command line parameters: host and port
'''

import requests
import json
import googlemaps

from flask import Flask, request, jsonify
from flask.ext.restful import Resource, Api, reqparse
from gensim.models.word2vec import Word2Vec as w
from gensim import utils, matutils
from numpy import exp, dot, zeros, outer, random, dtype, get_include, float32 as REAL,\
     uint32, seterr, array, uint8, vstack, argsort, fromstring, sqrt, newaxis, ndarray, empty, sum as np_sum
import argparse
import base64
import sys


import argparse
import json
import sys

import googleapiclient.discovery
import os
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/home/nishnik/hypertrack_backend/hypertrack-hackathon-d41bef2f7ec0.json"

gmapsKEY = 'A....M' # your key here

distance = 500
gmaps = googlemaps.Client(key=gmapsKEY)
from math import radians, cos, sin, asin, sqrt
def haversine(loc1, loc2):
    """
    Calculate the great circle distance between two points 
    on the earth (specified in decimal degrees)
    """
    # convert decimal degrees to radians 
    lat1, lon1 = loc1['lat'], loc1['lng']
    lat2, lon2 = loc2['lat'], loc2['lng']
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    # haversine formula 
    dlon = lon2 - lon1 
    dlat = lat2 - lat1 
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a)) 
    # Radius of earth in kilometers is 6371
    km = 6371* c
    return km

def find_places(loc, distance,to_find):
    gmaps = googlemaps.Client(key=gmapsKEY)
    print (loc, distance,to_find)
    found_places = gmaps.places_nearby(loc, distance,to_find)
    returned_places = []
    print (found_places)
    for place in found_places['results']:
        loc2 = place['geometry']['location']
        dist = haversine(loc, loc2)
        returned_places.append([dist, [place['name'], loc2]]) # add opening hours TODO
    returned_places.sort()
    return returned_places



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




user_tasks_dict = {}
user_tasks_keyword = {}
def filter_words(words):
    if words is None:
        return
    return [word for word in words if word in model.vocab]

user_name = "d52e224533b9e98f_ne"
class user_notice(Resource):
    def get(self):
        parser = reqparse.RequestParser()
        parser.add_argument('user_id', type=str, required=True, help="Word set 1 cannot be blank!", action='append')
        args = parser.parse_args()
        print ('came here')
        myheaders = {'Content-Type' : 'application/json', 'Authorization' : 'token sk_d42ac03d2e4a2db0039c0ff4505b2c2d7db85dda'}
        itemurl = 'https://api.hypertrack.com/api/v1/users/'
        response = requests.get(itemurl, headers=myheaders)
        all_users = json.loads(response.text)
        last_location = {}
        for user in all_users['results']:
            last_location[user['lookup_id']] = user['last_location']
        print (last_location)
        loc = last_location[user_name]['geojson']['coordinates']
        print (loc)
        new_loc = {}
        new_loc['lat'] = loc[1]
        new_loc['lng'] = loc[0]
        found_places = []
        if (not user_name in user_tasks_keyword):
            user_tasks_keyword[user_name] = []
        for keywords in user_tasks_keyword[user_name]:
            to_find = keywords
            print (to_find)
            found_places = find_places(new_loc, 1000, to_find)
        print (found_places)
        if (len(found_places)):
            return found_places[0][1][1]
        return found_places

class user_add_del(Resource):
    def post(self):
        parser = reqparse.RequestParser()
        parser.add_argument('user_id', type=str, required=True, help="id of the user")
        parser.add_argument('is_add', type=str, required=True, help="1 for add, else it is delete")
        parser.add_argument('task', type=str, required=True, help="description of task")
        args = parser.parse_args()
        try:
            user = args['user_id']
            print ('check')
            if (args['is_add'] == '1'):
                if (not user in user_tasks_dict):
                    user_tasks_dict[user] = []
                    user_tasks_keyword[user] = []
                if (not args['task'] in user_tasks_dict[user]):
                    user_tasks_dict[user].append(args['task'])
            else:
                user_tasks_dict[user].remove(args['task'])
            res = str(user_tasks_dict[user])
            user_tasks_keyword[user].append(analyze_entities(args['task']))
            print ('tasks of users', res)
            print ('keyword extracted', user_tasks_keyword)
            return res
        except Exception as e:
            print (e)
            return


app = Flask(__name__)
api = Api(app)

@app.errorhandler(404)
def pageNotFound(error):
    return "page not found"

@app.errorhandler(500)
def raiseError(error):
    return error

if __name__ == '__main__':
    global model

    #----------- Parsing Arguments ---------------
    host = "0.0.0.0"
    path = "/peace"
    port = 5000
    api.add_resource(user_notice, path+'/user_notice')
    api.add_resource(user_add_del, path+'/user_add_del')
    app.run(host=host, port=port)

# http://0.0.0.0:5000/peace/user_add_del?user_id=tempid&is_add=1&task=temp%20task
# http://0.0.0.0:5000/peace/user_notice?user_id=tempid