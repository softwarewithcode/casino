# casino


## About <br>
This repository contains modularized building blocks for a simple virtual casino and includes a blackjack and Texas Hold'em No Limit games. <br>

### Documentation
/utils/documentation

### Workspace instructions and technical
 /utils/workspace

### Frontend
Current frontends are merely a reference implementations for backend testing. Repo description contains a link which shows how the UI works without building a workspace. <br>
Frontend is reactive using Pinia and Vue combination. Frontend has no animations. <br>
Documentation section has more information about ui-backend websocket communications. It might be useful if you want to test/study WebSockets using this project. Or maybe build brand new user interface. 

### Backend
Java 19 with Virtual Threads preview / JakartaEE.  <br>

Some classes have Virtual Threads parts commented out for future. + ConcurrentPreviewTestBreaksWithoutConfiguration.java as Junit test class which uses Virtual Threads and Cyclic Barriers for concurrent testing.

### Future development ideas
Next game? Roulette, Omaha etc. <br>
Database handling, authentication. <br>

### For learning
On the backend side it would be really nice to hear if you find/see some bugs especially concurrency related but all are welcomed.





 