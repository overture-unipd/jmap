# Usage: locust -f test.py --host=http://localhost:8000
# Then open the web page: http://localhost:8089/

from locust import HttpUser, task, between
import json
import random

class Alice(HttpUser):

    domain = "jmaptest.duckdns.org"

    name = "alice"
    password = "al"
    email = name + "@" + domain

    with open("requests/identify.json") as f:
        identify = f.read()

    with open("requests/show_mailboxes.json") as f:
        show_mailboxes = f.read()

    with open("requests/first_email_query.json") as f:
        first_email_query = f.read()

    with open("requests/show_inbox.json") as f:
        show_inbox = f.read()

    with open("requests/email_query.json") as f:
        email_query = f.read()

    with open("requests/create_mailbox_test.json") as f:
        create_mailbox_test = f.read()

    with open("requests/destroy_mailbox_test.json") as f:
        destroy_mailbox_test = f.read()

 
    @task
    def test3(self):

        # Authenticate
        get_response = self.client.get("/.well-known/jmap", auth=(self.email, self.password)).text

        # Get accountId
        get_response = json.loads(get_response)
        account_id = list(get_response["accounts"].keys())[0]

        # Show identity
        self.identify = self.identify.replace("replace_here_accountId", account_id)
        self.client.post("/api/jmap", auth=(self.email, self.password), data=self.identify)

        # Show mailboxes
        self.show_mailboxes = self.show_mailboxes.replace("replace_here_accountId", account_id)
        show_mailboxes_response = self.client.post("/api/jmap", auth=(self.email, self.password), data=self.show_mailboxes).text

        # Get mailbox_id
        show_mailboxes_response = json.loads(show_mailboxes_response)
        mailbox_id = show_mailboxes_response["methodResponses"][0][1]["list"][0]["id"]

        # If exists, get mailbox_send_id
        mailbox_send_id = ""
        mailbox_list = show_mailboxes_response["methodResponses"][0][1]["list"]
        for mailbox in mailbox_list:
            if mailbox["name"] == "Sent":
                mailbox_send_id = mailbox["id"]
                break

        # Send first email query
        self.first_email_query = self.first_email_query.replace("replace_here_accountId", account_id)
        email_query_response = self.client.post("/api/jmap", auth=(self.email, self.password), data=self.first_email_query).text

        # Get session_state
        email_query_response = json.loads(email_query_response)
        session_state = email_query_response["sessionState"]

        # Show inbox
        self.show_inbox = self.show_inbox.replace("replace_here_accountId", account_id)
        self.show_inbox = self.show_inbox.replace("replace_here_inMailbox", mailbox_id)
        self.show_inbox = self.show_inbox.replace("replace_here_sessionState", session_state)
        show_inbox_response = self.client.post("/api/jmap", auth=(self.email, self.password), data=self.show_inbox).text

        # Get anchor
        show_inbox_response = json.loads(show_inbox_response)
        anchor = show_inbox_response["methodResponses"][3][1]["ids"][-1]

        # Send email query
        self.email_query = self.email_query.replace("replace_here_accountId", account_id)
        self.email_query = self.email_query.replace("replace_here_inMailbox", mailbox_id)
        self.email_query = self.email_query.replace("replace_here_anchor", anchor)
        email_query_response = self.client.post("/api/jmap", auth=(self.email, self.password), data=self.email_query)

        # Create mailbox test
        self.create_mailbox_test = self.create_mailbox_test.replace("replace_here_accountId", account_id)
        create_mailbox_test_response = self.client.post("/api/jmap", auth=(self.email, self.password), data=self.create_mailbox_test).text
        
        # Get mailbox_test_id
        create_mailbox_test_response = json.loads(create_mailbox_test_response)
        mailbox_test_id = create_mailbox_test_response["methodResponses"][0][1]["created"]["mb-test"]["id"]

        # Destroy mailbox test
        self.destroy_mailbox_test = self.destroy_mailbox_test.replace("replace_here_accountId", account_id)
        self.destroy_mailbox_test = self.destroy_mailbox_test.replace("replace_here_mailbox_test_id", mailbox_test_id)
        self.client.post("/api/jmap", auth=(self.email, self.password), data=self.destroy_mailbox_test)
        
