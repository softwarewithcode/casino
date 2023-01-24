# casino

## About <br>
This repository contains modularized building blocks for a simple virtual casino and includes a blackjack game. Users from different machines can see every players' cards and chips on the table just like playing a real game. Turn changes from player to player based on actions, timeouts and by received cards. <br>

### Documentation
/utils/documentation

### Workspace instructions and technical
 /utils/workspace

### Frontend
Current frontend is merely a reference implementation for backend testing. Repo description contains a link which shows how the UI works without building a workspace. <br>
Frontend is reactive using Pinia and Vue combination. Frontend has no animations. <br>
Documentation section has more information about ui-backend websocket communications. It might be useful if you want to test/study WebSockets using this project. Or maybe build brand new user interface. 

### Backend
Built originally using Java 19 with Virtual Threads / JakartaEE. Downgraded later to LTS Java17, point being to get the (backend) workspace running in a Docker container. Currently Tomee has only Java17 docker images available. Check issues tab with Docker. <br>

Some classes have Virtual Threads parts commented out for future. + ConcurrentPreviewTestBreaksWithoutConfiguration.java as Junit test class which uses Virtual Threads and Cyclic Barriers for concurrent testing.

### Future development ideas
Next game? Roulette, Texas Hold'em, Omaha etc. <br>
Database handling, authentication. <br>

### For learning
On the backend side it would be really nice to hear if you find/see some bugs especially concurrency related but all are welcomed.





 