Spring Version 21

Digital Wallet Application 

Implementation of a digital wallet supporting various features like transfers from wallet to wallet , creation of wallet.
Having a secure connection using jwt tokens for authentication. Its a Monolith application using PostgreSQL and Redis for storage.
Its currently build for small traffic thats why i went with the current setup.

Database Design  

<img width="680" height="588" alt="image" src="https://github.com/user-attachments/assets/a73318c5-61ba-4f25-b5ff-bc94a862ebf1" />

its currently designed for a one-to-one relationship between the wallet and the user , with further improvments will be upgraded to user one-to-many wallets



