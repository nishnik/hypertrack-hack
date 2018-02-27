import requests
import json
import googlemaps

myheaders = {'Content-Type' : 'application/json', 'Authorization' : 'token sk_d42ac03d2e4a2db0039c0ff4505b2c2d7db85dda'}
itemurl = 'https://api.hypertrack.com/api/v1/users/'
response = requests.get(itemurl, headers=myheaders)
all_users = json.loads(response.text)
last_location = {}
for user in all_users['results']:
    last_location[user['lookup_id']] = user['last_location']

# users_choices
user_choices = {}

gmaps = googlemaps.Client(key='AIzaSyCSovazAF57sGHFe10YKZIr58Vn1Dx0enM')
to_find = 'restaurant'
to_find = 'mobile phone charger'
# loc = {'lat': 28.7327431, 'lng': 77.1187878}
loc = {'lat': 28.7258326, 'lng': 77.1627518}
distance = 500

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
    found_places = gmaps.places_nearby(loc, distance,to_find)
    returned_places = []
    for place in found_places['results']:
        loc2 = place['geometry']['location']
        dist = haversine(loc, loc2)
        returned_places.append([dist, [place['name'], loc2]]) # add opening hours TODO
    returned_places.sort()
    return returned_places
