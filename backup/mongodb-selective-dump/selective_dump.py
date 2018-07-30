# Backup of MongoDB collections that are ESSENTIAL for a restore.
# arg1: MongoDB host (defaults to localhost)
# arg2: MongoDB port (defaults to 27017)

from pymongo import MongoClient
from subprocess import Popen
import shutil
import sys

interesting_collections = ['tests','companyData','inputs','units','tests','mocks','overrides','pressure_tasks','runs','vault','console','connectorInstances']

host = 'localhost'
port = 27017
print(sys.argv)
if len(sys.argv)>1:
    host = sys.argv[1]
if len(sys.argv)>2:
    port = int(sys.argv[2])

client = MongoClient(host,port)
databases = client.list_database_names()
client.close()
shutil.rmtree('./selective_dump', ignore_errors=True)
for d in databases:
    if d=='apipulse' or '00' in d:
        for coll in interesting_collections:
            process = Popen(['mongodump','--db',d,'--collection',coll,'--out','selective_dump'])
            process.wait()
