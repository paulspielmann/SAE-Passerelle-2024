CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE Parties (
    ID_partie SERIAL PRIMARY KEY,
    ID_utilisateur_blanc INTEGER REFERENCES Utilisateurs(ID_utilisateur),
    ID_utilisateur_noir INTEGER REFERENCES Utilisateurs(ID_utilisateur),
    Date DATE,
    Resultat VARCHAR(50),
    Coups TEXT -- ou JSON pour stocker les coups
);
