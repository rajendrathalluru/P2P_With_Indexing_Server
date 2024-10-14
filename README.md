# P2P_With_Indexing_Server

This Projects requirements can be found below


Requiremnts:

java,Websocket,Threads

In this project, I Will design a simple P2P system consisting of two components:
1) A Central Indexing Server
This server indexes the contents of all peers that register with it. It also provides a 
search facility for peers. In this simplified version, sophisticated search algorithms 
are not required—an exact match will suffice. At a minimum, the server should 
provide the following interface to the peer clients:
o register(peer id, file name, ...) — Invoked by a peer to register its files with the 
indexing server. The server then builds an index for the peer. While 
sophisticated algorithms like automatic indexing are not required, feel free to 
implement additional features as you see fit. You may also provide optional 
information, such as the client’s bandwidth, to make the server more 'real'.
o search(file name) — This function searches the index and returns all matching 
peers to the requestor.
o deregister(peer id, file name) — Invoked by a peer to delete a file entry from the 
index server. After registering files with the register function, a peer can use 
the deregister function to remove corresponding indexes for a file from the 
server.
2) A Peer
Each peer acts as both a client and a server. As a client, the user specifies a file name 
to search on the indexing server using search. The server returns a list of peers that 
2
hold the requested file. The user can choose one of these peers, and the client will 
connect to that peer to download the file. As a server, the peer waits for requests 
from other peers and sends the requested file upon receiving a request. At a 
minimum, the peer server should provide the following interface:
o retrieve(file name) — Invoked by a peer to download a file from another peer.



Extra Requirement for the project

Each peer should have an automatic update mechanism. If a user modifies or deletes 
files registered with the server, the changes should be reflected on the server 
promptly. For instance, if a user deletes a file, the indexing server should be notified 
and the corresponding entry should be removed from the index
