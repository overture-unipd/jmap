import csv
from rethinkdb import RethinkDB

db_host = 'localhost'
db_port = 28015
db_db = 'jmap'
domain = 'overture.duckdns.org'

r = RethinkDB()
r.connect(db_host, db_port, db=db_db).repl()

try:
    r.db_drop('jmap').run()
except:
    pass

r.db_create('jmap').run()

r.table_create('account').run()
r.table('account').index_create('address')
r.table_create('identity').run()
r.table('identity').index_create('account').run()
r.table_create('email').run()
r.table_create('mailbox').run()
# r.table('mailbox').index_create('account').run()
r.table_create('update').run()

with open('users.csv', mode='r') as f:
    csv_f = csv.DictReader(f)
    users = [{**x, 'state': 0} for x in csv_f]
    userids = r.table('account').insert(users).run()['generated_keys']

r.table('identity').insert(
    [{'account': userids[i], 'name': users['username'], 'address': users['username']+'@'+domain} for i, u in enumerate(userids)]
  ).run()

# TODO: minio bucket creation
# client.make_bucket("overture")
#   public void setupInbox() {
#     MailboxInfo m = new MailboxInfo(UUID.randomUUID().toString(), "Inbox", Role.INBOX, true);
#     insertMailbox(m.getId(), m);
#   }
