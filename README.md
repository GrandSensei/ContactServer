# ContactServer
The changes made from version 4.1




###### MAIN ADDITION ######
End-to-End encryption using RSA algo to send the hex key and AES for data communication.
Type of Contact and permissions to access only the contacts the client is allowed beforehand.
Shift from file backup to Database backup.



###### ADDITIONS NEEDED ######
1. A login method for differential access. Hardcoding the type access is a boring way.
2. Better loggers. File-based logging right now is pretty useless when multiple clients are accessing the server.
3. A local map storage or even a database on the client's side as client's backup
4. Timeout and refresh features in the client
5. Better error handling



###### Version History ######
S4.2, s4.3 were the result of reckless experiments. They are buggy as fuck no point discussing.

The two classes used are library-dependent and were in use in s4.4, but there is the class EncryptionStuff, which will replace these soon, hopefully.

In s4.5, the aim is to revamp the whole server to use TLS for a secure connection, removing the need for RSA and AES stuff. Now the processes of key handshakes and encrypting decrypting messages is
part of this server library.
The version s4.5.1 was a result of a major overhaul to shift from a standard Java program to a maven dependency one. Might seem small but makes it really easier to shift data backup from a text file
to a MySQL database.

S4.6 involves type groups and access configurations for the client. This is achieved by associating each contact with a type,
and the client having access to a particular type of contact. Right now it is hardcoded, but soon I will create a login mechanism.
This also marks the first attempt at transferring to a MySQL database for more robust data management.

S4.7 has introduced server push updates for the contacts. I got to figure out how to understand profilers and understand how much load my server can handle.
We resolved Delete method.
Are we done? Are we to resolve all our current bugs, and we are left to do is stress tests this? Is that all after so many of our efforts?
Perhaps. This marks the end of mark 4 ContactServer. A new generation is to be introduced after this.

###### Fixes From Mine ######
These problems are due to my own code edits arising after the version s.4.
The Contact class always had UUID being assigned randomly whenever a new Contact was generated. The problem arose in the Search method which generates a new Contact
while running its 'for loop'. The idea is to remove the UUID generator from Contact and transfer it to the Contact manager somehow.



###### Small fixes ######
1.
Before: Fixing the issue of ending connection due to error when loading the contactList when the client connects.
The contactList would load the very first contact but then die because it received an empty output which it couldn't parse.
This further resulted in issues of adding more contacts after just the first one.
Resolved: Adding an Empty type which is associated with empty outputs that is to be ignored by the client

2.
Before: Contact being invalidated when email or phone isn't mentioned
Resolved: Changes in the ContactValidator with eitherContact method.

3.
Before: Refresh not refreshing but creating duplicates
Resolved: Add a line to clear the contactList before sending a GET_MESSAGE request.

4.
Before: Adding a contact would generate a duplicate list in contactList
Resolved: Similar to 3.

5.
Before: The contacts would reset if any of them had either a missing phone or email.
Resolved: Editing the loadContacts method in ContactManager and Contact constructor to allow for missing out on either email or
phone.

6.
Before: The contact would still be valid even if the phone number is invalid.
Resolved: Use the similar line of codes for loadContacts and edit the ContactValidator.EitherContact to X0R rather than an OR return.
Resolved_s4.6: The XOR made it so if both the phone and email were added, it would result in invalid contact. We edit the validators to return false when empty and
improve on parsing which resolves the aggregated EitherContact method.

7.
Before: When two contacts share details, the search method shows the earliest one and then crashes the connection. Needs a better
        approach.
Resolved: Instead of sending a response from the server after the StringBuilder result crosses the while loop, we send a response within the loop, resetting the result for the next Contact.
Resolved_s4.6: Yeah, so by changing the storage from file to database, I just used the search to utilize that which resolves it pretty much by itself.

8.   
Before: When a contact has to be deleted, it would show errors, from s4.4 to s4.6 it was due to generation of new UUIDs for each contact searched and deleting those copies.
In s4.7 we shifted to databases which changed the issue from new UUIDs to incorrect parsing of String before applying the delete contact. 
Resolved: An updated deleteContact2 method was introduced and parsing was corrected. IT NOW DELETES A CONTACT LESGOOO.

###### Current errors ######
(Which I don't know how to fix)
1.
<Weeks ago>The delete method didn't work for some reason I am unaware about. My idea was using the search method in ContactManager
to find the contact and then get its uuid to delete it from the hashmap then send the rest of the contacts back.
<15/02/25>Well I figured out the major issue. Whenever you use the search method, It generates a new object Contact for each match it gets.
This results in the UUID getting changed due to how the Class Constructors work. I cannot figure out how to work around that.
<17/02/25> Gods this is frustrating.
<25/02/25> SOLVED!!!!!!!!!!!!!!@#$@#%!@#


2.
QQQ backdoor to shut down the server remotely from another client doesnt work...
