import csv
from rethinkdb import RethinkDB
from dotenv import load_dotenv
from os import environ
from minio import Minio
import uuid

load_dotenv('./.env')

rethinkdb_host = 'localhost'
rethinkdb_port = 28015
rethinkdb_db = environ.get('RETHINKDB_DB')

minio_host = 'localhost'
minio_port = 10000
minio_access = environ.get('MINIO_ACCESS')
minio_secret = environ.get('MINIO_SECRET')
minio_bucket = environ.get('MINIO_BUCKET')

domain = environ.get('DOMAIN')


r = RethinkDB()
r.connect(rethinkdb_host, rethinkdb_port, db=rethinkdb_db).repl()

try:
    r.db_drop('jmap').run()
except:
    pass

r.db_create('jmap').run()

r.table_create('account').run()
r.table('account').index_create('username').run()

r.table_create('identity').run()
r.table('identity').index_create('account').run()

r.table_create('email').run()
r.table('email').index_create('account').run()

r.table_create('emailsubmission').run()
r.table('emailsubmission').index_create('account').run()

r.table_create('mailbox').run()
r.table('mailbox').index_create('account').run()

r.table_create('update').run()
r.table('update').index_create('account').run()

with open('users.csv', mode='r') as f:
    csv_f = csv.DictReader(f)
    users = [{**x, 'username': x['name']+'@'+domain, 'name': x['name'], 'state': 0} for x in csv_f]
    userids = r.table('account').insert(users).run()['generated_keys']

r.table('identity').insert(
    [{'account': userids[i], 'name': users[i]['name'], 'email': users[i]['username']} for i, u in enumerate(userids)]
).run()

mailboxids = r.table('mailbox').insert(
    [{'account': userids[i], 'name': 'Inbox', 'role': 'inbox'} for i, u in enumerate(userids)]
).run()['generated_keys']

for i, u in enumerate(mailboxids):
    name = users[i]['name']
    address = users[i]['username']
    threadid = str(uuid.uuid4())
    r.table('email').insert(
        [{'account': userids[i], 'bodyStructure': {'partId': '0', 'type': 'text/plain'}, 'bodyValues': {'0': {'value': 'Lorem Ipsum'}}, 'from': [{'email': 'franz@abc.com', 'name': 'franz'}], 'id': str(uuid.uuid4()), 'mailboxIds': {mailboxids[i]: True}, 'preview': 'Lorem', 'receivedAt': '2024-03-16T18:32:20Z', 'sentAt': '2024-03-16T18:32:20+01:00', 'subject': 'Re: Aliquam', 'textBody': [{'partId': '0', 'type': 'text/plain'}], 'threadId': threadid, 'to': [{'email': address, 'name': name}]}, {'account': userids[i], 'bodyStructure': {'partId': '0', 'type': 'text/plain'}, 'bodyValues': {'0': {'value': 'Dolor Sit Amet'}}, 'from': [{'email': address, 'name': name}], 'id': str(uuid.uuid4()), 'mailboxIds': {mailboxids[i]: True}, 'preview': 'Dolor', 'receivedAt': '2024-03-16T17:30:20Z', 'sentAt': '2024-03-16T17:30:20+01:00', 'subject': 'Aliquam', 'textBody': [{'partId': '0', 'type': 'text/plain'}], 'threadId': threadid, 'to': [{'email': address, 'name': name}]}]
    ).run()

client = Minio(
    minio_host+':'+str(minio_port),
    access_key = minio_access,
    secret_key = minio_secret,
    secure = False
)

bucket_name = minio_bucket
try:
    client.make_bucket(bucket_name)
except Exception as err:
    pass
