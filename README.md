Digital Wallet Application 

Java Version 21 , Spring Boot Version 4 

Digital wallet supporting atomic cross-wallet transfers and wallet creation. The application features a secure JWT-based authentication layer and uses PostgreSQL for data storage and Redis for low-latency caching.

While currently architected as a monolith for optimal development speed, the design prioritizes data integrity through ACID-compliant transactions, ensuring a consistent ledger even during failed transfer attempts. The system is designed with a 1:N User-to-Wallet growth path in mind to support multi-currency portfolios.

Database Design  


<img width="680" height="588" alt="image" src="https://github.com/user-attachments/assets/a73318c5-61ba-4f25-b5ff-bc94a862ebf1" />


